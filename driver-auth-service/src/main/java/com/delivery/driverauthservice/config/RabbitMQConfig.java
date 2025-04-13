package com.delivery.driverauthservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

    public static final String DRIVER_REGISTRATION_QUEUE = "driver-registration-queue";
    public static final String DRIVER_REGISTRATION_EXCHANGE = "driver-registration-exchange";
    public static final String DRIVER_REGISTRATION_ROUTING_KEY = "driver.registration";

    public static final String DRIVER_SYNC_RESULT_QUEUE = "driver-sync-result-queue";
    public static final String DRIVER_SYNC_RESULT_EXCHANGE = "driver-sync-result-exchange";
    public static final String DRIVER_SYNC_RESULT_ROUTING_KEY = "driver.sync.result";

    @Bean
    public Queue driverRegistrationQueue() {
        return new Queue(DRIVER_REGISTRATION_QUEUE, true);
    }

    @Bean
    public Queue driverSyncResultQueue() {
        return new Queue(DRIVER_SYNC_RESULT_QUEUE, true);
    }

    @Bean
    public TopicExchange driverRegistrationExchange() {
        return new TopicExchange(DRIVER_REGISTRATION_EXCHANGE);
    }

    @Bean
    public TopicExchange driverSyncResultExchange() {
        return new TopicExchange(DRIVER_SYNC_RESULT_EXCHANGE);
    }

    @Bean
    public Binding driverRegistrationBinding(Queue driverRegistrationQueue,
                                             TopicExchange driverRegistrationExchange) {
        return BindingBuilder.bind(driverRegistrationQueue)
                .to(driverRegistrationExchange)
                .with(DRIVER_REGISTRATION_ROUTING_KEY);
    }

    @Bean
    public Binding driverSyncResultBinding(Queue driverSyncResultQueue,
                                           TopicExchange driverSyncResultExchange) {
        return BindingBuilder.bind(driverSyncResultQueue)
                .to(driverSyncResultExchange)
                .with(DRIVER_SYNC_RESULT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}