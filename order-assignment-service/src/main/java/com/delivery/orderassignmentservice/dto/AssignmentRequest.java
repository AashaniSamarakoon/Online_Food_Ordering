package com.delivery.orderassignmentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Driver ID is required")
    private Long driverId;
}