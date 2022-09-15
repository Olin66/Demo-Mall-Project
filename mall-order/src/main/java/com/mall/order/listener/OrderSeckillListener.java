package com.mall.order.listener;

import com.mall.common.to.mq.SeckillOrderTo;
import com.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSeckillListener {
    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo to, Channel channel, Message message) throws IOException {
        try {
            orderService.createSeckillOrder(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
