package com.delivery.orderassignmentservice.dto.notification;

import com.delivery.orderassignmentservice.dto.LocationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderAssignmentNotification {
    // Order identification
    private Long orderId;
    private String orderNumber;

    // Payment information
    private String payment;
    private String currency;

    // Time information
    private LocalDateTime expiryTime;
    private long timestamp;

    // Restaurant details
    private String restaurantName;
    private String restaurantAddress;

    // Customer details
    private String customerAddress;
    private String customerCity;

    // Option 1: Use LocationDTO objects
    private LocationDTO restaurantCoordinates;
    private LocationDTO customerCoordinates;

    // Option 2: Use individual coordinates (uncomment if you prefer this approach)
    // private double pickupLatitude;
    // private double pickupLongitude;
    // private double deliveryLatitude;
    // private double deliveryLongitude;

    // Optional fields for customer experience
    private String specialInstructions;
}