package com.delivery.driverauthservice.config;

import com.delivery.driverauthservice.messaging.DriverEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class MessagingConfig {

    @Bean
    public DriverEventPublisher driverEventPublisher(RabbitTemplate rabbitTemplate) {
        return new DriverEventPublisher(rabbitTemplate);
    }
}