package com.delivery.driverservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAcceptanceRequest {
    private Long driverId;
    private Long orderId;
    private boolean accepted;
    private String rejectionReason; // Optional, used if rejected
}