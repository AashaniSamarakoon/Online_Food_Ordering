package com.delivery.orderassignmentservice.service;

import com.delivery.orderassignmentservice.dto.OrderAssignmentDTO;
import com.delivery.orderassignmentservice.dto.AssignmentRequest;
import com.delivery.orderassignmentservice.dto.AssignmentStatusUpdate;
import com.delivery.orderassignmentservice.dto.OrderDetailsDTO;
import com.delivery.orderassignmentservice.dto.notification.OrderAssignmentNotification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderAssignmentService {
    OrderAssignmentDTO processOrderAssignment(Long orderId);
    OrderAssignmentDTO updateAssignmentStatus(AssignmentStatusUpdate update);
    void confirmAssignment(Long orderId, Long driverId);
    void handleRejection(Long orderId, Long driverId);
    List<OrderAssignmentDTO> getAssignmentsByOrder(Long orderId);
    List<OrderAssignmentDTO> getAssignmentsByDriver(Long driverId);
    /**
     * Get the active assignment for a driver with complete order details
     */
    OrderDetailsDTO getDriverActiveOrderDetails(Long driverId);
    List<OrderAssignmentNotification> getPendingAssignmentsForDriver(Long driverId);

}