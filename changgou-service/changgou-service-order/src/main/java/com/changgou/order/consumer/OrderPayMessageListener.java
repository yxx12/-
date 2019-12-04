package com.changgou.order.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 订单的监听器
 */
@Component
@RabbitListener(queues = {"${mq.pay.queue.order}"})  //可以配置多个队列
public class OrderPayMessageListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    //传递过来的消息是json串，所以用string 接收，这都想不到 脑浆湖了
    public void readMsg(String text) {
        //获取消息
        Map<String, String> map = JSON.parseObject(text, Map.class);
        //消费消息
        String return_code = map.get("return_code");  //通信状态，success才会有result_code
        if ("SUCCESS".equals(return_code)) {
            String result_code = map.get("result_code"); //交易标识
            String out_trade_no = map.get("out_trade_no");//订单号
            //如果交易成功 result_code=success,更新订单支付状态
            if ("SUCCESS".equals(result_code)){
                String transaction_id = map.get("transaction_id"); //交易流水号
                orderService.updateOrder(out_trade_no,transaction_id);
            }else {
                //支付失败，更新支付状态
                orderService.deleteOrder(out_trade_no);
            }
        }

    }


}
