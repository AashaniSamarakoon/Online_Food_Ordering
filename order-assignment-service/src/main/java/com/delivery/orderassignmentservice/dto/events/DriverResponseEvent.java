package com.delivery.orderassignmentservice.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DriverResponseEvent {
    private Long orderId;
    private Long driverId;
    private String status; // ACCEPTED/REJECTED
    private LocalDateTime responseTime;
}