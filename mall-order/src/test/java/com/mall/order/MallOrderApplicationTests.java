package com.mall.order;

import com.mall.order.entity.OrderReturnReasonEntity;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
class MallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void sendMessage() {
        OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
        entity.setId(1L);
        entity.setName("awd");
        entity.setCreateTime(new Date());
        rabbitTemplate.convertAndSend("java-exchange", "hello.java", entity);
    }

    @Test
    void createExchange() {
        amqpAdmin.declareExchange(new DirectExchange("java-exchange", true, false));
    }

    @Test
    void createQueue() {
        amqpAdmin.declareQueue(new Queue("java-queue", true, false, false));
    }

    @Test
    void createBinding() {
        amqpAdmin.declareBinding(new Binding("java-queue", Binding.DestinationType.QUEUE,
                "java-exchange", "hello.java", null));
    }

}
