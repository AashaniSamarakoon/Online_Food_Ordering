package com.delivery.orderassignmentservice.messaging;

import com.delivery.orderassignmentservice.dto.events.OrderCreatedEvent;
import com.delivery.orderassignmentservice.service.OrderAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderAssignmentConsumer {

    private final OrderAssignmentService orderAssignmentService;

    /**
     * Listen for order created events and initiate the assignment process
     */
    @RabbitListener(queues = "${rabbitmq.queue.order-created}")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order ID: {}", event.getOrderId());
        try {
            orderAssignmentService.processOrderAssignment(event.getOrderId());
            log.info("Order {} assigned successfully", event.getOrderId());
        } catch (Exception e) {
            log.error("Error processing order assignment for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
}