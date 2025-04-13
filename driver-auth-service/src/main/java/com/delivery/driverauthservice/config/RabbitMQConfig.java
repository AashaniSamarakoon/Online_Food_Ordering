package com.delivery.driverauthservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange:driver-exchange}")
    private String exchangeName;

    public static final String DRIVER_REGISTRATION_EXCHANGE = "driver.registration.exchange";
    public static final String DRIVER_REGISTRATION_QUEUE = "driver.registration.queue";
    public static final String DRIVER_REGISTRATION_ROUTING_KEY = "driver.registration";

    public static final String DRIVER_SYNC_RESULT_EXCHANGE = "driver.sync.result.exchange";
    public static final String DRIVER_SYNC_RESULT_QUEUE = "driver.sync.result.queue";
    public static final String DRIVER_SYNC_RESULT_ROUTING_KEY = "driver.sync.result";

    public static final String DRIVER_STATUS_EXCHANGE = "driver.status.exchange";
    public static final String DRIVER_STATUS_QUEUE = "driver.status.queue";
    public static final String DRIVER_STATUS_ROUTING_KEY = "driver.status";

    public static final String DRIVER_VERIFICATION_EXCHANGE = "driver.verification.exchange";
    public static final String DRIVER_VERIFICATION_QUEUE = "driver.verification.queue";
    public static final String DRIVER_VERIFICATION_ROUTING_KEY = "driver.verification";

    @Bean
    public DirectExchange driverRegistrationExchange() {
        return new DirectExchange(DRIVER_REGISTRATION_EXCHANGE);
    }

    @Bean
    public Queue driverRegistrationQueue() {
        return new Queue(DRIVER_REGISTRATION_QUEUE, true);
    }

    @Bean
    public Binding driverRegistrationBinding(Queue driverRegistrationQueue, DirectExchange driverRegistrationExchange) {
        return BindingBuilder.bind(driverRegistrationQueue)
                .to(driverRegistrationExchange)
                .with(DRIVER_REGISTRATION_ROUTING_KEY);
    }

    @Bean
    public DirectExchange driverSyncResultExchange() {
        return new DirectExchange(DRIVER_SYNC_RESULT_EXCHANGE);
    }

    @Bean
    public Queue driverSyncResultQueue() {
        return new Queue(DRIVER_SYNC_RESULT_QUEUE, true);
    }

    @Bean
    public Binding driverSyncResultBinding(Queue driverSyncResultQueue, DirectExchange driverSyncResultExchange) {
        return BindingBuilder.bind(driverSyncResultQueue)
                .to(driverSyncResultExchange)
                .with(DRIVER_SYNC_RESULT_ROUTING_KEY);
    }

    @Bean
    public DirectExchange driverStatusExchange() {
        return new DirectExchange(DRIVER_STATUS_EXCHANGE);
    }

    @Bean
    public Queue driverStatusQueue() {
        return new Queue(DRIVER_STATUS_QUEUE, true);
    }

    @Bean
    public Binding driverStatusBinding(Queue driverStatusQueue, DirectExchange driverStatusExchange) {
        return BindingBuilder.bind(driverStatusQueue)
                .to(driverStatusExchange)
                .with(DRIVER_STATUS_ROUTING_KEY);
    }

    @Bean
    public DirectExchange driverVerificationExchange() {
        return new DirectExchange(DRIVER_VERIFICATION_EXCHANGE);
    }

    @Bean
    public Queue driverVerificationQueue() {
        return new Queue(DRIVER_VERIFICATION_QUEUE, true);
    }

    @Bean
    public Binding driverVerificationBinding(Queue driverVerificationQueue, DirectExchange driverVerificationExchange) {
        return BindingBuilder.bind(driverVerificationQueue)
                .to(driverVerificationExchange)
                .with(DRIVER_VERIFICATION_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}