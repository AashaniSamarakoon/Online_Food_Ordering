package com.delivery.driverservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAssignmentRequest {
    private Long driverId;
    private Long orderId;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String customerName;
    private Double estimatedDistance;
    private Integer estimatedTimeMinutes;
    private Double orderAmount;
    private Double deliveryFee;
}