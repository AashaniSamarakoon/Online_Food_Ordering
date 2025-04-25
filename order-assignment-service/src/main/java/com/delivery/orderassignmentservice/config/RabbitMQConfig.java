package com.delivery.orderassignmentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.order-created}")
    private String orderCreatedQueue;

    @Value("${rabbitmq.queue.assignment-completed}")
    private String assignmentCompletedQueue;

    @Value("${rabbitmq.exchange.order}")
    private String orderExchange;

    @Value("${rabbitmq.routing-key.order-created}")
    private String orderCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.assignment-completed}")
    private String assignmentCompletedRoutingKey;

    // Queue definitions
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(orderCreatedQueue, true);
    }

    @Bean
    public Queue assignmentCompletedQueue() {
        return new Queue(assignmentCompletedQueue, true);
    }

    // Exchange
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(orderExchange);
    }

    // Bindings
    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(orderCreatedQueue)
                .to(orderExchange)
                .with(orderCreatedRoutingKey);
    }

    @Bean
    public Binding assignmentCompletedBinding(Queue assignmentCompletedQueue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(assignmentCompletedQueue)
                .to(orderExchange)
                .with(assignmentCompletedRoutingKey);
    }

    // Message converter
    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate with message converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}