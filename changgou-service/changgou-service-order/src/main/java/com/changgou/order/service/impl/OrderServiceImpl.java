package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.dao.OrderItemMapper;
import com.changgou.order.dao.OrderMapper;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import com.changgou.order.service.OrderService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/****
 * @Author:shenkunlin
 * @Description:Order业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired(required = false)
    private OrderMapper orderMapper;

    @Autowired(required = false)
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;
    @Autowired(required = false)
    private SkuFeign skuFeign;



    /**
     * 修改订单支付状态
     *
     * @param orderId
     * @param transactionId
     */
    @Override
    public void updateOrder(String orderId, String transactionId) {
        //从redis中获取订单，---->从mysql获取
        Order order = (Order) redisTemplate.boundHashOps("Order").get(orderId);
        if (order == null) {
            order = orderMapper.selectByPrimaryKey(orderId);
        }
        order.setUpdateTime(new Date());
        order.setPayTime(new Date());
        order.setTransactionId(transactionId);
        order.setPayStatus("1");
        orderMapper.updateByPrimaryKeySelective(order); //更新订单

        //删除redis中的订单
        redisTemplate.boundHashOps("Order").delete(orderId);
    }

    /**
     * 修改订单支付状态：失败
     * 实现；删除订单，回滚库存(此处不回滚订单了，因为本身设计就不合理，哪有已下单就减库存的，又不是秒杀活动)
     *
     * @param orderId
     */
    @Override
    public void deleteOrder(String orderId) {
        Order order = (Order) redisTemplate.boundHashOps("Order").get(orderId);
        if (order == null) {
            order = orderMapper.selectByPrimaryKey(orderId);
        }
        order.setPayStatus("2");
        order.setUpdateTime(new Date());
        orderMapper.updateByPrimaryKeySelective(order);
        redisTemplate.boundHashOps("Order").delete(orderId);
    }

    /**
     * 提交订单
     * 成功后直接调用支付接口
     * 增加Order
     *
     * @param order
     */
    @Override
    public Order add(Order order) {
        //获取购物车信息
        List<OrderItem> orderItems = cartService.list(order.getUsername());
        //保存订单
        long orderId = idWorker.nextId();
        order.setId(String.valueOf(orderId));
        int totalNum = 0;
        int totalMoney = 0;
        int totalPayMoney = 0;
        for (OrderItem orderItem : orderItems) {

            totalNum += orderItem.getNum();
            totalMoney += orderItem.getMoney();
            totalPayMoney += orderItem.getPayMoney();
        }
        order.setTotalNum(totalNum);
        order.setTotalMoney(totalMoney);
        order.setPayMoney(totalPayMoney);
        order.setPreMoney(totalMoney - totalPayMoney);  // 优惠金额
        order.setCreateTime(new Date());    // 下单时间
        order.setUpdateTime(new Date());    // 修改订单时间
        order.setBuyerRate("0");            // 是否评价：0，未评价 1：已评价
        order.setSourceType("1");           // 订单来源
        order.setOrderStatus("0");          // 订单状态
        order.setPayStatus("0");            // 订单支付状态
        order.setConsignStatus("0");        // 订单是否发货
        order.setPayType("1");              //默认在线支付
        //Mysql保存订单
        orderMapper.insertSelective(order);
        //在先支付：保存订单到redis
        if ("1".equals(order.getPayType())) {
            redisTemplate.boundHashOps("Order").put(order.getId(), order);
        }
        //保存订单明细
        for (OrderItem orderItem : orderItems) {
            long id = idWorker.nextId();
            orderItem.setId(String.valueOf(id));
            orderItem.setOrderId(order.getId());
            orderItemMapper.insertSelective(orderItem);
            //扣减库存(只需要商品id，和购买数量num不就行啦，在这儿直传过去不就行了)
            skuFeign.decr(orderItem.getSkuId(), orderItem.getNum());
            //删除购物车
            redisTemplate.boundHashOps("Cart_" + order.getUsername()).delete(orderItem.getSkuId());
        }

        return order;
    }


    /**
     * Order条件+分页查询
     *
     * @param order 查询条件
     * @param page  页码
     * @param size  页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Order> findPage(Order order, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(order);
        //执行搜索
        return new PageInfo<Order>(orderMapper.selectByExample(example));
    }

    /**
     * Order分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Order> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<Order>(orderMapper.selectAll());
    }

    /**
     * Order条件查询
     *
     * @param order
     * @return
     */
    @Override
    public List<Order> findList(Order order) {
        //构建查询条件
        Example example = createExample(order);
        //根据构建的条件查询数据
        return orderMapper.selectByExample(example);
    }


    /**
     * Order构建查询对象
     *
     * @param order
     * @return
     */
    public Example createExample(Order order) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (order != null) {
            // 订单id
            if (!StringUtils.isEmpty(order.getId())) {
                criteria.andEqualTo("id", order.getId());
            }
            // 数量合计
            if (!StringUtils.isEmpty(order.getTotalNum())) {
                criteria.andEqualTo("totalNum", order.getTotalNum());
            }
            // 金额合计
            if (!StringUtils.isEmpty(order.getTotalMoney())) {
                criteria.andEqualTo("totalMoney", order.getTotalMoney());
            }
            // 优惠金额
            if (!StringUtils.isEmpty(order.getPreMoney())) {
                criteria.andEqualTo("preMoney", order.getPreMoney());
            }
            // 邮费
            if (!StringUtils.isEmpty(order.getPostFee())) {
                criteria.andEqualTo("postFee", order.getPostFee());
            }
            // 实付金额
            if (!StringUtils.isEmpty(order.getPayMoney())) {
                criteria.andEqualTo("payMoney", order.getPayMoney());
            }
            // 支付类型，1、在线支付、0 货到付款
            if (!StringUtils.isEmpty(order.getPayType())) {
                criteria.andEqualTo("payType", order.getPayType());
            }
            // 订单创建时间
            if (!StringUtils.isEmpty(order.getCreateTime())) {
                criteria.andEqualTo("createTime", order.getCreateTime());
            }
            // 订单更新时间
            if (!StringUtils.isEmpty(order.getUpdateTime())) {
                criteria.andEqualTo("updateTime", order.getUpdateTime());
            }
            // 付款时间
            if (!StringUtils.isEmpty(order.getPayTime())) {
                criteria.andEqualTo("payTime", order.getPayTime());
            }
            // 发货时间
            if (!StringUtils.isEmpty(order.getConsignTime())) {
                criteria.andEqualTo("consignTime", order.getConsignTime());
            }
            // 交易完成时间
            if (!StringUtils.isEmpty(order.getEndTime())) {
                criteria.andEqualTo("endTime", order.getEndTime());
            }
            // 交易关闭时间
            if (!StringUtils.isEmpty(order.getCloseTime())) {
                criteria.andEqualTo("closeTime", order.getCloseTime());
            }
            // 物流名称
            if (!StringUtils.isEmpty(order.getShippingName())) {
                criteria.andEqualTo("shippingName", order.getShippingName());
            }
            // 物流单号
            if (!StringUtils.isEmpty(order.getShippingCode())) {
                criteria.andEqualTo("shippingCode", order.getShippingCode());
            }
            // 用户名称
            if (!StringUtils.isEmpty(order.getUsername())) {
                criteria.andLike("username", "%" + order.getUsername() + "%");
            }
            // 买家留言
            if (!StringUtils.isEmpty(order.getBuyerMessage())) {
                criteria.andEqualTo("buyerMessage", order.getBuyerMessage());
            }
            // 是否评价
            if (!StringUtils.isEmpty(order.getBuyerRate())) {
                criteria.andEqualTo("buyerRate", order.getBuyerRate());
            }
            // 收货人
            if (!StringUtils.isEmpty(order.getReceiverContact())) {
                criteria.andEqualTo("receiverContact", order.getReceiverContact());
            }
            // 收货人手机
            if (!StringUtils.isEmpty(order.getReceiverMobile())) {
                criteria.andEqualTo("receiverMobile", order.getReceiverMobile());
            }
            // 收货人地址
            if (!StringUtils.isEmpty(order.getReceiverAddress())) {
                criteria.andEqualTo("receiverAddress", order.getReceiverAddress());
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if (!StringUtils.isEmpty(order.getSourceType())) {
                criteria.andEqualTo("sourceType", order.getSourceType());
            }
            // 交易流水号
            if (!StringUtils.isEmpty(order.getTransactionId())) {
                criteria.andEqualTo("transactionId", order.getTransactionId());
            }
            // 订单状态,0:未完成,1:已完成，2：已退货
            if (!StringUtils.isEmpty(order.getOrderStatus())) {
                criteria.andEqualTo("orderStatus", order.getOrderStatus());
            }
            // 支付状态,0:未支付，1：已支付，2：支付失败
            if (!StringUtils.isEmpty(order.getPayStatus())) {
                criteria.andEqualTo("payStatus", order.getPayStatus());
            }
            // 发货状态,0:未发货，1：已发货，2：已收货
            if (!StringUtils.isEmpty(order.getConsignStatus())) {
                criteria.andEqualTo("consignStatus", order.getConsignStatus());
            }
            // 是否删除
            if (!StringUtils.isEmpty(order.getIsDelete())) {
                criteria.andEqualTo("isDelete", order.getIsDelete());
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
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Order
     *
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKey(order);
    }


    /**
     * 根据ID查询Order
     *
     * @param id
     * @return
     */
    @Override
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Order全部数据
     *
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }
}
