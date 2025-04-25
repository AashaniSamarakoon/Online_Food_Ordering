package com.delivery.orderassignmentservice.messaging;

import com.delivery.orderassignmentservice.dto.events.AssignmentCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderAssigmentProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.order}")
    private String orderExchange;

    @Value("${rabbitmq.routing-key.assignment-completed}")
    private String assignmentCompletedRoutingKey;

    /**
     * Send a message when an assignment is completed
     */
    public void sendAssignmentCompletedEvent(AssignmentCompletedEvent event) {
        rabbitTemplate.convertAndSend(orderExchange, assignmentCompletedRoutingKey, event);
    }
}