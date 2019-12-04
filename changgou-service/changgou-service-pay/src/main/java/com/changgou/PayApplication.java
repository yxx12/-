package com.changgou;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableEurekaClient
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }

    @Autowired
    private Environment env;

    // 创建普通队列
    @Bean
    public Queue orderQueue() {
        return new Queue(env.getProperty("mq.pay.queue.order"), true);

    }

    // 创建秒杀队列
    @Bean
    public Queue seckillOrderQueue(){
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"), true);
    }

    // 创建交换机
    @Bean
    public Exchange basicExchange() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"), true, false);
    }

    // 绑定普通队列
    @Bean
    public Binding bindQueueToExchange(Queue orderQueue, Exchange basicExchange) {
        return BindingBuilder.bind(orderQueue).to(basicExchange).with(env.getProperty("mq.pay.routing.orderkey")).noargs();
    }

    // 绑定秒杀队列
    @Bean
    public Binding queueBindingToExchangeForSeckillOrder(Queue seckillOrderQueue, Exchange basicExchange){
        return BindingBuilder.bind(seckillOrderQueue).to(basicExchange).with(env.getProperty("mq.pay.routing.seckillorderkey")).noargs();
    }



    /**
     * 到期数据队列
     * @return
     */
    @Bean
    public Queue seckillOrderTimerQueue() {
        return new Queue(env.getProperty("mq.pay.queue.seckillordertimer"), true);
    }

    /**
     * 超时数据队列
     * @return
     */
    @Bean
    public Queue delaySeckillOrderTimerQueue() {
        return QueueBuilder.durable(env.getProperty("mq.pay.queue.seckillordertimerdelay"))              //生产者发送消息的routingkey
                .withArgument("x-dead-letter-exchange", env.getProperty("mq.pay.exchange.order"))        // 消息超时进入死信队列，绑定死信队列交换机
                .withArgument("x-dead-letter-routing-key", env.getProperty("mq.pay.queue.seckillordertimer"))   // 绑定指定的routing-key
                .build();
    }

    /***
     * 交换机与队列绑定
     * @return
     */
    @Bean
    public Binding basicBinding() {
        return BindingBuilder.bind(seckillOrderTimerQueue()).to(basicExchange())
                .with(env.getProperty("mq.pay.queue.seckillordertimer")).noargs();
    }

}
