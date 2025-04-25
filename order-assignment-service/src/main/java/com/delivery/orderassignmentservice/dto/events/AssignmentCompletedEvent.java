package com.delivery.orderassignmentservice.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentCompletedEvent implements Serializable {
    private Long orderId;
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String status;
    private LocalDateTime assignedAt;
    private LocalDateTime estimatedArrivalTime;
}