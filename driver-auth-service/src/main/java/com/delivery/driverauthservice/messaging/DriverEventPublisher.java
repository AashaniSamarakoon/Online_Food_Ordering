package com.delivery.driverauthservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class DriverEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:driver-exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key.registration:driver.registration}")
    private String registrationRoutingKey;

    @Value("${rabbitmq.routing-key.status:driver.status}")
    private String statusRoutingKey;

    @Value("${rabbitmq.routing-key.verification:driver.verification}")
    private String verificationRoutingKey;

    /**
     * Publishes a driver registration event to the driver management service
     */
    public void publishDriverRegistration(DriverRegistrationEvent event) {
        try {
            log.info("Publishing driver registration event for driver: {}", event.getUsername());
            rabbitTemplate.convertAndSend(exchange, registrationRoutingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish driver registration event: {}", e.getMessage(), e);
        }
    }

    /**
     * Publishes a driver status update event (login/logout) to the driver management service
     */
    public void publishDriverStatusUpdate(Map<String, Object> event) {
        try {
            log.info("Publishing driver status update event");
            rabbitTemplate.convertAndSend(exchange, statusRoutingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish driver status update event: {}", e.getMessage(), e);
        }
    }

    /**
     * Publishes a driver verification event to the driver management service
     */
    public void publishDriverVerification(Map<String, Object> event) {
        try {
            log.info("Publishing driver verification event");
            rabbitTemplate.convertAndSend(exchange, verificationRoutingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish driver verification event: {}", e.getMessage(), e);
        }
    }
}