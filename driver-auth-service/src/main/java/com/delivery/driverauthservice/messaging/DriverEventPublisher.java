package com.delivery.driverauthservice.messaging;

import com.delivery.driverauthservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishDriverRegistration(DriverRegistrationEvent event) {
        log.info("Publishing driver registration event for tempId: {}", event.getTempDriverId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DRIVER_REGISTRATION_EXCHANGE,
                RabbitMQConfig.DRIVER_REGISTRATION_ROUTING_KEY,
                event
        );
    }
}