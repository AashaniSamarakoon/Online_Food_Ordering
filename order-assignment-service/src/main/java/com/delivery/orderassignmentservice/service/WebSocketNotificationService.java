package com.delivery.orderassignmentservice.service;

import com.delivery.orderassignmentservice.dto.events.DriverAssignmentEvent;

/**
 * Service for sending real-time notifications to drivers via WebSocket
 */
public interface WebSocketNotificationService {

    /**
     * Send an order assignment notification to a driver
     * @param event The driver assignment event containing order details
     */
    void sendOrderAssignmentToDriver(DriverAssignmentEvent event);
}