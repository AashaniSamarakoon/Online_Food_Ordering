package com.delivery.orderassignmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String status;
    private String customerName;
    private String customerPhone;

    private double pickupLatitude;
    private double pickupLongitude;
    private String pickupAddress;

    private double dropoffLatitude;
    private double dropoffLongitude;
    private String dropoffAddress;

    private LocalDateTime createdAt;
    private LocalDateTime estimatedDeliveryTime;

    // Additional fields as needed for your application
}