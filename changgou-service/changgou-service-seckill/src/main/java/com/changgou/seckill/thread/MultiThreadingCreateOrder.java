package com.changgou.seckill.thread;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import entity.IdWorker;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MultiThreadingCreateOrder {
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Async // 异步请求
    public void createOrder() {


        try {
            // 从队列中取出用户的下单信息
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();
            String time = seckillStatus.getTime();
            Long seckillId = seckillStatus.getGoodsId();
            String userId = seckillStatus.getUsername();

            // 下单，需要从队列中减掉（弹pop）一个商品id
            Object goodsId = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillId).rightPop();
            if (goodsId == null) {
                //删除重复排队信息
                redisTemplate.boundHashOps("UserQueueCount").delete(userId);
                //删除下单状态信息
                redisTemplate.boundHashOps("UserQueueStatus").delete(userId);
                //队列中没有商品Id，说明售罄，结束
                return;
            }
            // 1、获取商品信息(redis)
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(seckillId);
            if (seckillGoods == null || seckillGoods.getStockCount() <= 0) {
                throw new RuntimeException("对不起，该商品已售罄");
            }

            // 2、提交【保存】订单（保存到redis）
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());          // 主键
            seckillOrder.setSeckillId(seckillId);           // 商品id
            seckillOrder.setMoney(seckillGoods.getPrice()); // 商品单价
            seckillOrder.setUserId(userId);                 // 用户
            seckillOrder.setCreateTime(new Date());         // 提交订单的日期
            seckillOrder.setStatus("0");                    // 支付状态：未支付
            redisTemplate.boundHashOps("SeckillOrder").put(userId, seckillOrder);

            // 3、扣减库存
//            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);   // 扣减库存
            Long goodsCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillId, -1);
            seckillGoods.setStockCount(goodsCount.intValue());
            if (goodsCount <= 0) {
                // 该商品售卖完
                // 删除redis中的商品
                redisTemplate.boundHashOps("SeckillGoods_" + time).delete(seckillId);
                // 将商品同步到mysql中 1---特步---0（mysql）
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
            } else {
                // 该商品还有
                redisTemplate.boundHashOps("SeckillGoods_" + time).put(seckillId, seckillGoods);
            }
            // 4、下单成功后，需要更新订单的状态信息
            seckillStatus.setStatus(2);                                     // 订单的状态：等待支付
            seckillStatus.setMoney(Float.valueOf(seckillOrder.getMoney())); // 订单金额
            seckillStatus.setOrderId(seckillOrder.getId());                 // 订单id
            redisTemplate.boundHashOps("UserQueueStatus").put(userId, seckillStatus);



        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /***
     * 发送延时消息到RabbitMQ中
     * @param seckillStatus
     */
    public void sendTimerMessage(SeckillStatus seckillStatus) {
        rabbitTemplate.convertAndSend(env.getProperty("mq.pay.queue.seckillordertimerdelay"), (Object) JSON.toJSONString(seckillStatus), new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("10000");
                return message;
            }
        });
    }
}


