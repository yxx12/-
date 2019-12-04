package com.changgou.seckill.service;

import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.github.pagehelper.PageInfo;

import java.text.ParseException;
import java.util.List;

/****
 * @Author:shenkunlin
 * @Description:SeckillOrder业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface SeckillOrderService {

    /**
     * 交易成功更新订单状态
     * @param out_trade_no
     * @param transaction_id
     * @param username
     * @param time_end
     */
    void updateStatus(String out_trade_no, String transaction_id, String username, String time_end) throws ParseException;

    /**
     * 交易失败，删除redis订单
     * @param username
     */
    void deleteOrder(String username);
    /**
     * 查询抢单的状态
     * @param userId
     * @return
     */
    SeckillStatus queryStatus(String userId);

    /**
     * 下单秒杀
     * @param time   时间短
     * @param userId    用户名
     * @param seckillId 商品id
     * @return
     */
    Boolean add (String time, Long seckillId, String userId);
    /***
     * SeckillOrder多条件分页查询
     * @param seckillOrder
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size);

    /***
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(int page, int size);

    /***
     * SeckillOrder多条件搜索方法
     * @param seckillOrder
     * @return
     */
    List<SeckillOrder> findList(SeckillOrder seckillOrder);

    /***
     * 删除SeckillOrder
     * @param id
     */
    void delete(Long id);

    /***
     * 修改SeckillOrder数据
     * @param seckillOrder
     */
    void update(SeckillOrder seckillOrder);

    /***
     * 新增SeckillOrder
     * @param seckillOrder
     */
    void add(SeckillOrder seckillOrder);

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
     SeckillOrder findById(Long id);

    /***
     * 查询所有SeckillOrder
     * @return
     */
    List<SeckillOrder> findAll();



}
