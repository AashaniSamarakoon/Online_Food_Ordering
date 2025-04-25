package com.delivery.orderassignmentservice.service;

import com.delivery.orderassignmentservice.dto.OrderAssignmentDTO;
import com.delivery.orderassignmentservice.dto.AssignmentRequest;
import com.delivery.orderassignmentservice.dto.AssignmentStatusUpdate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderAssignmentService {
    @Transactional
    OrderAssignmentDTO processOrderAssignment(Long orderId);

    OrderAssignmentDTO createAssignment(AssignmentRequest request);
    OrderAssignmentDTO updateAssignmentStatus(AssignmentStatusUpdate update);
    List<OrderAssignmentDTO> getAssignmentsByOrder(Long orderId);
    List<OrderAssignmentDTO> getAssignmentsByDriver(Long driverId);
}