package com.delivery.orderassignmentservice.service.impl;

import com.delivery.orderassignmentservice.client.OrderServiceClient;
import com.delivery.orderassignmentservice.dto.LocationDTO;
import com.delivery.orderassignmentservice.dto.OrderDetailsDTO;
import com.delivery.orderassignmentservice.dto.events.DriverAssignmentEvent;
import com.delivery.orderassignmentservice.dto.notification.OrderAssignmentNotification;
import com.delivery.orderassignmentservice.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final OrderServiceClient orderServiceClient;

    @Override
    public void sendOrderAssignmentToDriver(DriverAssignmentEvent event) {
        try {
            // Get complete order details
            OrderDetailsDTO orderDetails = orderServiceClient.getOrderDetails(event.getOrderId());

            // Create the notification with necessary fields for driver's UI
            OrderAssignmentNotification notification = buildNotificationFromOrderDetails(event, orderDetails);

            // Debug log to verify the notification object
            log.debug("Sending notification to driver {}: {}", event.getDriverId(), notification);

            // Send to the specific driver's topic
            String destination = "/queue/driver." + event.getDriverId() + ".assignments";
            messagingTemplate.convertAndSend(destination, notification);

            log.info("Sent order assignment notification to driver {} for order {}",
                    event.getDriverId(), event.getOrderId());

        } catch (Exception e) {
            log.error("Error sending order assignment notification: {}", e.getMessage(), e);
        }
    }

    private OrderAssignmentNotification buildNotificationFromOrderDetails(
            DriverAssignmentEvent event, OrderDetailsDTO orderDetails) {

        // Format the total amount with currency symbol
        String paymentAmount = orderDetails.getTotalPrice() != null
                ? orderDetails.getTotalPrice().toString()
                : "0.00";

        // Check if coordinates exist, if not create empty ones
        LocationDTO restaurantCoords = orderDetails.getRestaurantCoordinates();
        LocationDTO customerCoords = orderDetails.getCustomerCoordinates();

        // Create location DTOs for the notification
        LocationDTO restaurantLocation = new LocationDTO(
                restaurantCoords != null ? restaurantCoords.getLatitude() : 0.0,
                restaurantCoords != null ? restaurantCoords.getLongitude() : 0.0
        );

        LocationDTO customerLocation = new LocationDTO(
                customerCoords != null ? customerCoords.getLatitude() : 0.0,
                customerCoords != null ? customerCoords.getLongitude() : 0.0
        );

        return OrderAssignmentNotification.builder()
                .orderId(orderDetails.getId())
                .orderNumber("ORD-" + orderDetails.getId())  // Generate an order number
                .payment(paymentAmount)
                .currency("LKR")
                // Restaurant details
                .restaurantName(orderDetails.getRestaurantName())
                .pickupAddress(orderDetails.getRestaurantAddress())
                // Customer details
                .deliveryAddress(orderDetails.getAddress())
                .customerName(orderDetails.getUsername())
                .phoneNumber(orderDetails.getPhoneNumber())
                // Coordinates as LocationDTO objects
                .restaurantCoordinates(restaurantLocation)
                .customerCoordinates(customerLocation)
                // Additional order details
                .deliveryFee(orderDetails.getDeliveryCharges())
                .specialInstructions("")
                // Assignment expiry time
                .expiryTime(event.getExpiryTime())
                .timestamp(System.currentTimeMillis())
                .build();
    }
}