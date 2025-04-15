package com.delivery.driverservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletionRequest {
    private Long driverId;
    private Long orderId;
    private Double actualDistance;
    private Integer actualTimeMinutes;
}