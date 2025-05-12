package com.delivery.orderassignmentservice.messaging;

import com.delivery.orderassignmentservice.dto.events.DriverResponseEvent;
import com.delivery.orderassignmentservice.service.OrderAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverResponseConsumer {

    private final OrderAssignmentService orderAssignmentService;

    /**
     * Listen for driver responses (accept/reject)
     */
    @RabbitListener(queues = "${rabbitmq.queue.driver-response}")
    public void handleDriverResponse(DriverResponseEvent event) {
        log.info("Received response from driver {} for order {}: {}",
                event.getDriverId(), event.getOrderId(), event.getStatus());

        try {
            if ("ACCEPTED".equals(event.getStatus())) {
                orderAssignmentService.confirmAssignment(
                        event.getOrderId(),
                        event.getDriverId()
                );
            } else {
                orderAssignmentService.handleRejection(
                        event.getOrderId(),
                        event.getDriverId()
                );
            }
        } catch (Exception e) {
            log.error("Error processing driver response: {}", e.getMessage());
        }
    }
}