package com.siae.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routingKey}")
    private String routingKey;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(String message, String to, String subject, String username) {
        EmailMessage emailMessage = new EmailMessage(to, subject, message, username);
        rabbitTemplate.convertAndSend(exchange, routingKey, emailMessage);
        System.out.println("Message sent to RabbitMQ: " + message);
    }
}
