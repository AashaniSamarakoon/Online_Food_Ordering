package com.delivery.orderassignmentservice.service.impl;

import com.delivery.orderassignmentservice.client.DriverServiceClient;
import com.delivery.orderassignmentservice.client.OrderServiceClient;
import com.delivery.orderassignmentservice.client.TrackingServiceClient;
import com.delivery.orderassignmentservice.dto.*;
import com.delivery.orderassignmentservice.dto.events.AssignmentCompletedEvent;
import com.delivery.orderassignmentservice.exception.AssignmentNotFoundException;
import com.delivery.orderassignmentservice.messaging.OrderAssigmentProducer;
import com.delivery.orderassignmentservice.model.OrderAssignment;
import com.delivery.orderassignmentservice.repository.OrderAssignmentRepository;
import com.delivery.orderassignmentservice.service.OrderAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderAssignmentServiceImpl implements OrderAssignmentService {

    private final OrderAssignmentRepository assignmentRepository;
    private final ModelMapper modelMapper;
    private final DriverServiceClient driverServiceClient;
    private final OrderServiceClient orderServiceClient;
    private final TrackingServiceClient trackingServiceClient;
    private final OrderAssigmentProducer rabbitMQProducer; // New RabbitMQ producer

    /**
     * Process a new order for assignment
     * This implements the sequence diagram flow for order assignment
     */
    @Override
    @Transactional
    public OrderAssignmentDTO processOrderAssignment(Long orderId) {
        log.info("Starting assignment process for order: {}", orderId);
        // 1. Get order details from Order Service
        OrderDTO orderDetails = orderServiceClient.getOrderDetails(orderId);

        // 2. Get nearby available drivers from Tracking Service
        List<DriverLocationDTO> nearbyDrivers = trackingServiceClient.getNearbyDrivers(
                orderDetails.getPickupLatitude(),
                orderDetails.getPickupLongitude(),
                5000, // default radius 5km
                10    // default limit 10 drivers
        );

        // 3. Run assignment logic to select best driver
        Optional<DriverLocationDTO> selectedDriver = selectBestDriver(nearbyDrivers, orderDetails);

        if (!selectedDriver.isPresent()) {
            log.warn("No available drivers found for order: {}", orderId);
            throw new RuntimeException("No available drivers found for this order");
        }

        DriverLocationDTO bestDriver = selectedDriver.get();
        log.info("Selected driver {} for order {}", bestDriver.getDriverId(), orderId);

        // 4. Create the assignment
        OrderAssignment assignment = OrderAssignment.builder()
                .orderId(orderId)
                .driverId(Long.parseLong(bestDriver.getDriverId()))
                .status("ASSIGNED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderAssignment savedAssignment = assignmentRepository.save(assignment);

        // 5. Update order status in Order Service
        orderServiceClient.updateOrderStatus(orderId);

        // 6. Notify Driver Service about the assignment
        OrderAssignmentDTO assignmentDTO = modelMapper.map(savedAssignment, OrderAssignmentDTO.class);
        driverServiceClient.assignOrderToDriver(Long.parseLong(bestDriver.getDriverId()), assignmentDTO);

        // 7. Get complete driver details for response
        DriverDTO driverInfo = driverServiceClient.getDriverDetails(Long.parseLong(bestDriver.getDriverId()));
        assignmentDTO.setDriver(modelMapper.map(driverInfo, OrderAssignmentDTO.AssignDriverDTO.class));

        // 8. Send assignment completed event via RabbitMQ
        sendAssignmentCompletedEvent(savedAssignment, driverInfo, orderDetails);

        return assignmentDTO;
    }

    /**
     * Build and send an assignment completed event
     */
    private void sendAssignmentCompletedEvent(OrderAssignment assignment, DriverDTO driver, OrderDTO order) {
        // Calculate estimated arrival time (simplified version)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime estimatedArrival = now.plusMinutes(15); // Simplified calculation

        AssignmentCompletedEvent event = AssignmentCompletedEvent.builder()
                .orderId(assignment.getOrderId())
                .driverId(assignment.getDriverId())
                .driverName(driver.getName())
                .driverPhone(driver.getPhone())
                .status(assignment.getStatus())
                .assignedAt(assignment.getCreatedAt())
                .estimatedArrivalTime(estimatedArrival)
                .build();

        rabbitMQProducer.sendAssignmentCompletedEvent(event);
        log.info("Sent assignment completed event for order: {}", assignment.getOrderId());
    }

    // Rest of the implementation remains the same

    private Optional<DriverLocationDTO> selectBestDriver(List<DriverLocationDTO> nearbyDrivers, OrderDTO order) {
        // Implementation unchanged from previous response
        if (nearbyDrivers == null || nearbyDrivers.isEmpty()) {
            return Optional.empty();
        }

        return nearbyDrivers.stream()
                .sorted((d1, d2) -> {
                    double dist1 = calculateDistance(
                            d1.getLatitude(), d1.getLongitude(),
                            order.getPickupLatitude(), order.getPickupLongitude()
                    );
                    double dist2 = calculateDistance(
                            d2.getLatitude(), d2.getLongitude(),
                            order.getPickupLatitude(), order.getPickupLongitude()
                    );
                    return Double.compare(dist1, dist2);
                })
                .findFirst();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Implementation unchanged from previous response
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000; // convert to meters
    }


    @Override
    @Transactional
    public OrderAssignmentDTO createAssignment(AssignmentRequest request) {
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

        // Notify driver service
        OrderAssignmentDTO assignmentDTO = modelMapper.map(savedAssignment, OrderAssignmentDTO.class);
        driverServiceClient.assignOrderToDriver(request.getDriverId(), assignmentDTO);

        // Build response with driver details
        OrderAssignmentDTO response = modelMapper.map(savedAssignment, OrderAssignmentDTO.class);
        response.setDriver(modelMapper.map(driverInfo, OrderAssignmentDTO.AssignDriverDTO.class));

        return response;
    }

    @Override
    @Transactional
    public OrderAssignmentDTO updateAssignmentStatus(AssignmentStatusUpdate update) {
        // Your existing implementation...
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
        // Your existing implementation...
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
        // Your existing implementation...
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