package com.changgou.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Map;

/**
 * 秒杀订单监听器
 * 用来监听微信支付的回调mq信息
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
public class SeckillOrderPayMessageListener {
    @Autowired
    private SeckillOrderService seckillOrderService;


    /**
     * 监听MQ
     * @param text
     */
    @RabbitHandler
    public void readSeckillOrderMessage(String text) throws ParseException {
        Map<String,String> map = JSON.parseObject(text, Map.class);
        //获取交易标识
        String return_code = map.get("return_code");
        if ("SUCCESS".equals(return_code)){             //通信标识
            //获取商家附加信息
            String attach = map.get("attach");
            Map<String,String> attachMap = JSON.parseObject(attach, Map.class);
            //获取用户名
            String username = attachMap.get("username");
            //获取交易标识
            String result_code = map.get("result_code");//交易成功标识
            if ("SUCCESS".equals(result_code)){
                //更新订单
                String out_trade_no = map.get("out_trade_no");
                String transaction_id = map.get("transaction_id");
                String time_end = map.get("time_end");
                seckillOrderService.updateStatus(out_trade_no, transaction_id, username, time_end);
            }else {
                // 删除订单
                seckillOrderService.deleteOrder(username);
            }
        }
    }

}
