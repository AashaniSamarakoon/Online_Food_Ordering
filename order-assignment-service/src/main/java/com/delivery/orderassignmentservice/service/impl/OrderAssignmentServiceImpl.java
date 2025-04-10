package com.delivery.orderassignmentservice.service.impl;

import com.delivery.orderassignmentservice.client.DriverServiceClient;
import com.delivery.orderassignmentservice.dto.*;
import com.delivery.orderassignmentservice.exception.AssignmentNotFoundException;
import com.delivery.orderassignmentservice.model.OrderAssignment;
import com.delivery.orderassignmentservice.repository.OrderAssignmentRepository;
import com.delivery.orderassignmentservice.service.OrderAssignmentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderAssignmentServiceImpl implements OrderAssignmentService {

    private final OrderAssignmentRepository assignmentRepository;
    private final ModelMapper modelMapper;
    private final DriverServiceClient driverServiceClient;

    @Override
    @Transactional
    public OrderAssignmentDTO createAssignment(AssignmentRequest request) {
        // Verify driver is available
        driverServiceClient.checkDriverAvailability(request.getDriverId());

        // Get driver details
        DriverDTO driverInfo = driverServiceClient.getDriverDetails(request.getDriverId());

        OrderAssignment assignment = OrderAssignment.builder()
                .orderId(request.getOrderId())
                .driverId(request.getDriverId())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderAssignment savedAssignment = assignmentRepository.save(assignment);

        // Build response with driver details
        OrderAssignmentDTO response = modelMapper.map(savedAssignment, OrderAssignmentDTO.class);
        response.setDriver(modelMapper.map(driverInfo, OrderAssignmentDTO.AssignDriverDTO.class));

        return response;
    }

    @Override
    @Transactional
    public OrderAssignmentDTO updateAssignmentStatus(AssignmentStatusUpdate update) {
        OrderAssignment assignment = assignmentRepository.findById(update.getAssignmentId())
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found"));

        assignment.setStatus(update.getStatus());
        assignment.setUpdatedAt(LocalDateTime.now());
        OrderAssignment updatedAssignment = assignmentRepository.save(assignment);

        DriverDTO driverInfo = driverServiceClient.getDriverDetails(updatedAssignment.getDriverId());

        // Build response
        OrderAssignmentDTO response = modelMapper.map(updatedAssignment, OrderAssignmentDTO.class);
        response.setDriver(modelMapper.map(driverInfo, OrderAssignmentDTO.AssignDriverDTO.class));

        return response;

    }

    @Override
    public List<OrderAssignmentDTO> getAssignmentsByOrder(Long orderId) {
        List<OrderAssignment> assignments = assignmentRepository.findByOrderId(orderId);

        return assignments.stream().map(assignment -> {
            // Get driver details for each assignment
            DriverDTO driverInfo = driverServiceClient.getDriverDetails(assignment.getDriverId());

            // Map to response DTO
            OrderAssignmentDTO response = modelMapper.map(assignment, OrderAssignmentDTO.class);
            response.setDriver(modelMapper.map(driverInfo, OrderAssignmentDTO.AssignDriverDTO.class));

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<OrderAssignmentDTO> getAssignmentsByDriver(Long driverId) {
        // Get driver details once (since all assignments are for same driver)
        DriverDTO driverInfo = driverServiceClient.getDriverDetails(driverId);

        List<OrderAssignment> assignments = assignmentRepository.findByDriverId(driverId);

        return assignments.stream().map(assignment -> {
            // Map to response DTO
            OrderAssignmentDTO response = modelMapper.map(assignment, OrderAssignmentDTO.class);
            response.setDriver(modelMapper.map(driverInfo, OrderAssignmentDTO.AssignDriverDTO.class));

            return response;
        }).collect(Collectors.toList());
    }
}