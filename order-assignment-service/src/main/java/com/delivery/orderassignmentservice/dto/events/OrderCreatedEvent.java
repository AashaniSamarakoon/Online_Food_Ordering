package com.delivery.orderassignmentservice.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {
    private Long orderId;
    private double pickupLatitude;
    private double pickupLongitude;
    private double dropoffLatitude;
    private double dropoffLongitude;
    private String customerName;
    private String customerPhone;
    private LocalDateTime timestamp;
}