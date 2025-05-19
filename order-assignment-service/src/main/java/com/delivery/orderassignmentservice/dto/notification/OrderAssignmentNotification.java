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
    private String pickupAddress;  // Using restaurantAddress as pickupAddress

    // Customer details
    private String deliveryAddress;  // Using customer address as deliveryAddress
    private String customerName;     // Added customer name
    private String phoneNumber;      // Added phone number

    // Coordinates for navigation
    private LocationDTO restaurantCoordinates;
    private LocationDTO customerCoordinates;

    // Order details
    private Double deliveryFee;
    private String specialInstructions;  // For any special delivery instructions
}