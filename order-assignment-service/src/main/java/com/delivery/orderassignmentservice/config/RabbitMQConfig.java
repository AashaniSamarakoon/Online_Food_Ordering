package com.delivery.orderassignmentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.order}")
    private String orderExchange;

    // Queue names
    @Value("${rabbitmq.queue.order-created}")
    private String orderCreatedQueue;

    @Value("${rabbitmq.queue.driver-notification}")
    private String driverNotificationQueue;

    @Value("${rabbitmq.queue.driver-response}")
    private String driverResponseQueue;

    // Routing keys
    @Value("${rabbitmq.routing-key.order-created}")
    private String orderCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.assignment-completed}")
    private String assignmentCompletedRoutingKey;

    @Value("${rabbitmq.routing-key.driver-notification}")
    private String driverNotificationRoutingKey;

    @Value("${rabbitmq.routing-key.driver-response}")
    private String driverResponseRoutingKey;

    // Create message converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Configure RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // Create exchange
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(orderExchange);
    }

    // Create queues
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(orderCreatedQueue);
    }

    @Bean
    public Queue driverNotificationQueue() {
        return new Queue(driverNotificationQueue);
    }

    @Bean
    public Queue driverResponseQueue() {
        return new Queue(driverResponseQueue);
    }

    // Bind queues to exchange
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(orderExchange())
                .with(orderCreatedRoutingKey);
    }

    @Bean
    public Binding driverNotificationBinding() {
        return BindingBuilder
                .bind(driverNotificationQueue())
                .to(orderExchange())
                .with(driverNotificationRoutingKey);
    }

    @Bean
    public Binding driverResponseBinding() {
        return BindingBuilder
                .bind(driverResponseQueue())
                .to(orderExchange())
                .with(driverResponseRoutingKey);
    }
}