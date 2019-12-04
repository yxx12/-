package com.changgou.canal.mq.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicQueue {

    public static final String TOPIC_QUEUE_SPU = "topic.queue.spu";
    public static final String TOPIC_EXCHANGE_SPU = "topic.exchange.spu";

    /**
     * Topic模式 SPU变更队列
     * @return
     */
    @Bean
    public Queue topicQueueSpu() {
        return new Queue(TOPIC_QUEUE_SPU);
    }

    /***
     * SPU队列交换机
     * @return
     */
    @Bean
    public TopicExchange topicSpuExchange() {
        return new TopicExchange(TOPIC_EXCHANGE_SPU);
    }

    /***
     * 队列绑定交换机
     * @return
     */
    @Bean
    public Binding topicQueueBind(Queue topicQueue, Exchange topicExchange) {
        return BindingBuilder.bind(topicQueue).to(topicExchange).with(TOPIC_QUEUE_SPU).noargs();
    }
}