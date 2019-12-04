package com.changgou.seckill.service.impl;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.seckill.thread.MultiThreadingCreateOrder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/****
 * @Author:shenkunlin
 * @Description:SeckillOrder业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired(required = false)
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

    @Override
    public void updateStatus(String out_trade_no, String transaction_id, String username, String time_end) throws ParseException {
        //获取订单
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        //更新订单数据并且将订单储存在mysql中
        if (seckillOrder != null) {
            //更新订单数据
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmss");
            Date payTime = sdf.parse(time_end);
            seckillOrder.setPayTime(payTime);               //支付完成时间
            seckillOrder.setTransactionId(transaction_id);  //交易流水
            seckillOrder.setStatus("1");                    //支付状态，已更新

            //订单存储到mysql
            seckillOrderMapper.insertSelective(seckillOrder);
            System.out.println("交易成功，支付时间："+seckillOrder.getPayTime()+"支付状态1是已支付："+seckillOrder.getStatus()+"交易流水"+seckillOrder.getTransactionId());
            //redis  删除订单，删除重复下单，删除订单状态
            redisTemplate.boundHashOps("SeckillOrder").delete(username);
            redisTemplate.boundHashOps("UserOrderQueue").delete(username);
            redisTemplate.boundHashOps("UserQueueStatus").delete(username);

        }
    }

    @Override
    public void deleteOrder(String username) {
        //获取订单状态及订单信息
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        if (seckillOrder != null && seckillStatus != null) {        //已下单，未支付
            //删除订单
            redisTemplate.boundHashOps("SeckillOrder").delete(username);
            //回滚商品的库存
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + seckillStatus.getTime()).get(seckillStatus.getGoodsId());
            if (seckillGoods == null) {
                seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
                // 将商品id添加到队列中
                redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).leftPush(seckillStatus.getGoodsId());
                      //加上秒杀商品的库存数量
                Long stockCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillStatus.getGoodsId(), 1);
                      //设置商品的库存
                seckillGoods.setStockCount(stockCount.intValue());
                      //恢复商品列表数据
                redisTemplate.boundHashOps("SeckillGoods_" + seckillStatus.getTime()).put(seckillStatus.getGoodsId().toString(), seckillGoods);
                // 删除排队信息、抢单状态信息
                redisTemplate.boundHashOps("UserQueueCount").delete(username);
                redisTemplate.boundHashOps("UserQueueStatus").delete(username);
            }
        }
    }

    @Override
    public SeckillStatus queryStatus(String userId) {
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(userId);
        return seckillStatus;
    }

    /**
     * 下单秒杀(调用异步处理)
     *
     * @param time      时间短
     * @param userId    用户名
     * @param seckillId 商品id
     * @return
     */
    @Override
    public Boolean add(String time, Long seckillId, String userId) {

        // 防止用户多次下单（不允许多次排队）：计数器
        Long userQueueCount = redisTemplate.boundHashOps("UserQueueCount").increment(userId, 1);
        if (userQueueCount > 1) {
            throw new RuntimeException("对不起，不允许重复排队---下单");
        }
        // 用户下单信息状态信息封装pojo，放入队列中
        SeckillStatus seckillStatus = new SeckillStatus(userId, new Date(), 1, seckillId, time);
        redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);

        // 将用户的订单的状态信息存储到redis中
        redisTemplate.boundHashOps("UserQueueStatus").put(userId, seckillStatus);

        // 业务1
        multiThreadingCreateOrder.createOrder();

        // TODO 业务2
        return true;
    }

    /**
     * SeckillOrder条件+分页查询
     *
     * @param seckillOrder 查询条件
     * @param page         页码
     * @param size         页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     *
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder) {
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     *
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder) {
        Example example = new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if (seckillOrder != null) {
            // 主键
            if (!StringUtils.isEmpty(seckillOrder.getId())) {
                criteria.andEqualTo("id", seckillOrder.getId());
            }
            // 秒杀商品ID
            if (!StringUtils.isEmpty(seckillOrder.getSeckillId())) {
                criteria.andEqualTo("seckillId", seckillOrder.getSeckillId());
            }
            // 支付金额
            if (!StringUtils.isEmpty(seckillOrder.getMoney())) {
                criteria.andEqualTo("money", seckillOrder.getMoney());
            }
            // 用户
            if (!StringUtils.isEmpty(seckillOrder.getUserId())) {
                criteria.andEqualTo("userId", seckillOrder.getUserId());
            }
            // 创建时间
            if (!StringUtils.isEmpty(seckillOrder.getCreateTime())) {
                criteria.andEqualTo("createTime", seckillOrder.getCreateTime());
            }
            // 支付时间
            if (!StringUtils.isEmpty(seckillOrder.getPayTime())) {
                criteria.andEqualTo("payTime", seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if (!StringUtils.isEmpty(seckillOrder.getStatus())) {
                criteria.andEqualTo("status", seckillOrder.getStatus());
            }
            // 收货人地址
            if (!StringUtils.isEmpty(seckillOrder.getReceiverAddress())) {
                criteria.andEqualTo("receiverAddress", seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if (!StringUtils.isEmpty(seckillOrder.getReceiverMobile())) {
                criteria.andEqualTo("receiverMobile", seckillOrder.getReceiverMobile());
            }
            // 收货人
            if (!StringUtils.isEmpty(seckillOrder.getReceiver())) {
                criteria.andEqualTo("receiver", seckillOrder.getReceiver());
            }
            // 交易流水
            if (!StringUtils.isEmpty(seckillOrder.getTransactionId())) {
                criteria.andEqualTo("transactionId", seckillOrder.getTransactionId());
            }
        }
        return example;
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     *
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     *
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }


}
