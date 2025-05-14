package com.delivery.orderassignmentservice.dto;

import lombok.Data;

@Data
public class OrderStatusUpdate {
    private String status;
    private Long driverId;
    private String failureReason;
}