package com.delivery.orderassignmentservice.messaging;

import com.delivery.orderassignmentservice.dto.events.AssignmentCompletedEvent;
import com.delivery.orderassignmentservice.dto.events.DriverAssignmentEvent;
import com.delivery.orderassignmentservice.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderAssignmentProducer {

    private final RabbitTemplate rabbitTemplate;
    private final WebSocketNotificationService webSocketNotificationService;

    @Value("${rabbitmq.exchange.order}")
    private String orderExchange;

    @Value("${rabbitmq.routing-key.assignment-completed}")
    private String assignmentCompletedRoutingKey;

    @Value("${rabbitmq.routing-key.driver-notification}")
    private String driverNotificationRoutingKey;

    /**
     * Send assignment completion event
     */
    public void sendAssignmentCompletedEvent(AssignmentCompletedEvent event) {
        rabbitTemplate.convertAndSend(orderExchange, assignmentCompletedRoutingKey, event);
    }

    /**
     * Send driver notification (parallel notifications)
     */
    public void sendDriverNotification(DriverAssignmentEvent event) {
        // Send through RabbitMQ for system events
        rabbitTemplate.convertAndSend(orderExchange, driverNotificationRoutingKey, event);

        // Also send through WebSocket for real-time UI updates
        webSocketNotificationService.sendOrderAssignmentToDriver(event);
    }
}