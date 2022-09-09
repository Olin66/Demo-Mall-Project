package com.mall.order.config;

import com.mall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @RabbitListener(queues = "order.release.order.queue")
    public void listen(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println(entity);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange", "order-event-exchange");
        map.put("x-dead-letter-routing-key", "order.release.order");
        map.put("x-message-ttl", 60000);
        return new Queue("order.delay.queue", true, false, false, map);
    }

    @Bean
    public Queue orderReleaseQueue() {
        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Binding createOrderBinding() {
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.create.order", null);
    }

    @Bean
    public Binding releaseOrderBinding() {
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.release.order", null);
    }
}
