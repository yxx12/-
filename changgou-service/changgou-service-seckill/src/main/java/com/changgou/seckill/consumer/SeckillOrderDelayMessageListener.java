package com.changgou.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.wxPay.feign.WeixinPayFeign;
import entity.Result;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * 监听mq消息
 * 订单是否超时（超时就删了）
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.seckillordertimer}")
public class SeckillOrderDelayMessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired(required = false)
    private WeixinPayFeign weixinPayFeign;

    /***
     * 读取消息
     * 判断Redis中是否存在对应的订单
     * 如果存在（说名超时未支付），则关闭支付，再关闭订单
     * @param message
     */
    @RabbitHandler
    public void consumeMessage(@Payload String message){
        //读取消息
      //  SeckillStatus seckillStatus = JSON.parseObject(message,SeckillStatus.class);
        Map<String,Object> map = JSON.parseObject(message, Map.class);
        //获取Redis中订单信息
      //  String username = seckillStatus.getUsername();
        String attach = (String) map.get("attach");
        String username = (String) JSON.parseObject(attach,Map.class).get("username");
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);

        //如果Redis中有订单信息，说明用户未支付
        if(seckillOrder!=null){
            System.out.println(new Date() +"准备回滚---"+username);
            //关闭支付
            String out_trade_no = (String) map.get("out_trade_no");
            Result closeResult = weixinPayFeign.closePay(Long.parseLong(out_trade_no));
            Map<String,String> closeMap = (Map<String, String>) closeResult.getData();

            if(closeMap!=null && closeMap.get("return_code").equalsIgnoreCase("success") &&
                    closeMap.get("result_code").equalsIgnoreCase("success") ){
                //到了此处说明，微信已经关闭了支付通道
                //商家可以删除订单了
                seckillOrderService.deleteOrder(username);
            }
        }
    }
}