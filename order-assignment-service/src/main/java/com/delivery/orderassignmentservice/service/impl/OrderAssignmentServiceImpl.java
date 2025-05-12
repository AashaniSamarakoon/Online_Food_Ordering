package com.delivery.orderassignmentservice.service.impl;

import com.delivery.orderassignmentservice.client.DriverServiceClient;
import com.delivery.orderassignmentservice.client.OrderServiceClient;
import com.delivery.orderassignmentservice.client.TrackingServiceClient;
import com.delivery.orderassignmentservice.dto.*;
import com.delivery.orderassignmentservice.dto.events.AssignmentCompletedEvent;
import com.delivery.orderassignmentservice.dto.events.DriverAssignmentEvent;
import com.delivery.orderassignmentservice.dto.notification.OrderAssignmentNotification;
import com.delivery.orderassignmentservice.exception.AssignmentNotFoundException;
import com.delivery.orderassignmentservice.exception.NoAvailableDriversException;
import com.delivery.orderassignmentservice.exception.OrderNotFoundException;
import com.delivery.orderassignmentservice.messaging.OrderAssignmentProducer;
import com.delivery.orderassignmentservice.model.OrderAssignment;
import com.delivery.orderassignmentservice.repository.OrderAssignmentRepository;
import com.delivery.orderassignmentservice.service.OrderAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final OrderAssignmentProducer rabbitMQProducer; // New RabbitMQ producer

    /**
     * Process a new order for assignment
     */
    @Override
    @Transactional
    public OrderAssignmentDTO processOrderAssignment(Long orderId) {
        // 1. Fetch order details
        OrderDetailsDTO order = orderServiceClient.getOrderDetails(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order " + orderId + " not found");
        }

        // 2. Find nearby drivers with dynamic radius
        int radius = calculateSearchRadius(orderId);
        List<DriverLocationDTO> nearbyDrivers = trackingServiceClient.getNearbyDrivers(
                order.getRestaurantCoordinates().getLatitude(),
                order.getRestaurantCoordinates().getLongitude(),
                radius
        );

        if (nearbyDrivers.isEmpty()) {
            throw new NoAvailableDriversException("No drivers available within " + radius + "m");
        }

        // 3. Create a list of drivers with their details and locations
        List<DriverWithDetails> driversWithDetails = new ArrayList<>();
        for (DriverLocationDTO location : nearbyDrivers) {
            // Get driver details including rating
            DriverDTO driverDetails = driverServiceClient.getDriverDetails(location.getDriverId());
            driversWithDetails.add(new DriverWithDetails(location, driverDetails));
        }

        // 4. Sort drivers by distance and rating
        List<DriverWithDetails> candidates = driversWithDetails.stream()
                .sorted(
                        Comparator.comparingDouble((DriverWithDetails d) -> d.getLocation().getDistance())
                                .thenComparingDouble(d -> -d.getDriverDetails().getRating()) // Descending rating
                )
                .limit(3)
                .toList();

        // 5. Send notifications to selected drivers
        candidates.forEach(driver -> {
            DriverAssignmentEvent event = new DriverAssignmentEvent(
                    orderId,
                    driver.getLocation().getDriverId(),
                    new LocationDTO(order.getRestaurantCoordinates().getLatitude(), order.getRestaurantCoordinates().getLongitude()),
                    driver.getLocation().getDistance(),
                    driver.getLocation().getHeading(),
                    LocalDateTime.now().plusSeconds(15)
            );
            rabbitMQProducer.sendDriverNotification(event);
        });

        // 6. Create pending assignment
        OrderAssignment assignment = new OrderAssignment();
        assignment.setOrderId(orderId);
        assignment.setCandidateDrivers(
                candidates.stream()
                        .map(driver -> driver.getLocation().getDriverId())
                        .collect(Collectors.toList())
        );
        assignment.setStatus("PENDING");
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment.setExpiryTime(LocalDateTime.now().plusSeconds(15));

        return modelMapper.map(assignmentRepository.save(assignment), OrderAssignmentDTO.class);
    }


    /**
     * Build and send an assignment completed event
     */
    private void sendAssignmentCompletedEvent(OrderAssignment assignment, DriverDTO driver, OrderDetailsDTO order) {
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


    private int calculateSearchRadius(Long orderId) {
        int rejectionCount = assignmentRepository.countRejectionsByOrder(orderId);
        return rejectionCount >= 3 ? 8000 : 5000; // 8km after 3 rejects
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

    @Transactional
    @Override
    public void confirmAssignment(Long orderId, Long driverId) {  // Changed from String to Long
        log.info("Confirming assignment for order {} by driver {}", orderId, driverId);

        // Validate and update assignment
        OrderAssignment assignment = assignmentRepository.findByOrderIdAndDriverId(orderId, driverId)
                .orElseThrow(() -> new AssignmentNotFoundException(
                        "No pending assignment found for order " + orderId + " and driver " + driverId));

        if (!"PENDING".equals(assignment.getStatus())) {
            throw new IllegalStateException("Assignment is not in PENDING state");
        }

        // Update assignment status
        assignment.setStatus("CONFIRMED");
        assignment.setUpdatedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        // Update order and driver statuses

        // Create OrderStatusUpdate object
        OrderStatusUpdate orderStatusUpdate = new OrderStatusUpdate();
        orderStatusUpdate.setStatus("DRIVER_CONFIRMED");
        orderStatusUpdate.setDriverId(driverId);

        // Call the updateOrderStatus method with the proper object
        orderServiceClient.updateOrderStatus(orderId, orderStatusUpdate);

        // Create a DriverStatusUpdate object
        DriverStatusUpdate statusUpdate = new DriverStatusUpdate();
        statusUpdate.setDriverId(driverId);
        statusUpdate.setStatus("BUSY");
        statusUpdate.setLastActiveAt(LocalDateTime.now());

        driverServiceClient.updateDriverStatus(driverId, statusUpdate);

        // Cancel other pending assignments for this order
        Optional<OrderAssignment> pendingAssignmentOpt = assignmentRepository.findByOrderIdAndStatus(orderId, "PENDING");
        if (pendingAssignmentOpt.isPresent()) {
            OrderAssignment pendingAssignment = pendingAssignmentOpt.get();
            pendingAssignment.setStatus("CANCELLED");
            pendingAssignment.setUpdatedAt(LocalDateTime.now());
            assignmentRepository.save(pendingAssignment);
        }

        // Send completion event
        DriverDTO driver = driverServiceClient.getDriverDetails(driverId);
        OrderDetailsDTO order = orderServiceClient.getOrderDetails(orderId);
        sendAssignmentCompletedEvent(assignment, driver, order);
    }

    @Transactional
    @Override
    public void handleRejection(Long orderId, Long driverId) {
        log.info("Handling rejection for order {} by driver {}", orderId, driverId);

        // Mark assignment as rejected
        Optional<OrderAssignment> assignmentOpt = assignmentRepository.findByOrderIdAndDriverId(orderId, driverId);
        assignmentOpt.ifPresent(assignment -> {
            assignment.setStatus("REJECTED");
            assignment.setUpdatedAt(LocalDateTime.now());
            assignmentRepository.save(assignment);
        });

        // Check rejection count
        long rejectionCount = assignmentRepository.countByOrderIdAndStatus(orderId, "REJECTED");

        // Get the original order details
        OrderDetailsDTO order = orderServiceClient.getOrderDetails(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        // Try next candidate or restart process
        Optional<OrderAssignment> pendingAssignment = assignmentRepository.findByOrderIdAndStatus(orderId, "PENDING");
        if (pendingAssignment.isPresent()) {
            // Try next candidate driver
            List<Long> candidates = pendingAssignment.get().getCandidateDrivers();
            Optional<Long> nextDriver = candidates.stream()
                    .filter(candidateId ->
                            !assignmentRepository.existsByOrderIdAndDriverIdAndStatus(
                                    orderId, candidateId, "REJECTED"))
                    .findFirst();

            if (nextDriver.isPresent()) {
                notifySingleDriver(orderId, nextDriver.get(), order);
            } else {
                restartAssignmentProcess(orderId, order, rejectionCount);
            }
        } else {
            restartAssignmentProcess(orderId, order, rejectionCount);
        }
    }

    private void notifySingleDriver(Long orderId, Long driverId, OrderDetailsDTO order) {
        DriverLocationDTO driver = trackingServiceClient.getDriverLocation(driverId);
        DriverAssignmentEvent event = new DriverAssignmentEvent(
                orderId,
                driverId,
                new LocationDTO(order.getRestaurantCoordinates().getLatitude(), order.getRestaurantCoordinates().getLongitude()),
                driver.getDistance(),
                driver.getHeading(),
                LocalDateTime.now().plusSeconds(15)
        );
        rabbitMQProducer.sendDriverNotification(event);
    }

    private void restartAssignmentProcess(Long orderId, OrderDetailsDTO order, long rejectionCount) {
        int radius = rejectionCount >= 3 ? 8000 : 5000;
        log.info("Restarting assignment process for order {} with radius {}m", orderId, radius);

        List<DriverLocationDTO> nearbyDrivers = trackingServiceClient.getNearbyDrivers(
                order.getRestaurantCoordinates().getLatitude(),
                order.getRestaurantCoordinates().getLongitude(),
                radius
        );

        // Filter out previously rejected drivers
        List<Long> rejectedDrivers = assignmentRepository.findByOrderId(orderId).stream()
                .map(OrderAssignment::getDriverId)
                .toList();

        List<DriverLocationDTO> availableDrivers = nearbyDrivers.stream()
                .filter(driver -> !rejectedDrivers.contains(driver.getDriverId()))
                .toList();

        if (!availableDrivers.isEmpty()) {
            processOrderAssignment(orderId); // Restart the process
        } else {
            log.warn("No available drivers left for order {}", orderId);

            // Create OrderStatusUpdate object
            OrderStatusUpdate statusUpdate = new OrderStatusUpdate();
            statusUpdate.setStatus("FAILED");
            statusUpdate.setFailureReason("No drivers available");

            // Call the updateOrderStatus method with the proper object
            orderServiceClient.updateOrderStatus(orderId, statusUpdate);
        }
    }

    @Override
    public OrderDetailsDTO getDriverActiveOrderDetails(Long driverId) {
        // Find the driver's active assignment
        OrderAssignment assignment = assignmentRepository.findByDriverIdAndStatus(driverId, "CONFIRMED")
                .orElseThrow(() -> new AssignmentNotFoundException(
                        "No active assignment found for driver " + driverId));

        // Get detailed order information
        OrderDetailsDTO orderDetails = orderServiceClient.getOrderDetails(assignment.getOrderId());

        // Enrich with assignment information if needed
        orderDetails.setAssignmentTime(assignment.getCreatedAt());

        return orderDetails;
    }

    @Override
    public List<OrderAssignmentNotification> getPendingAssignmentsForDriver(Long driverId) {
        // Find any pending assignments for this driver
        List<OrderAssignment> pendingAssignments = assignmentRepository.findAllByDriverIdAndStatus(driverId, "PENDING");

        // Convert assignments to notifications
        return pendingAssignments.stream().map(assignment -> {
            // Get order details
            OrderDetailsDTO order = orderServiceClient.getOrderDetails(assignment.getOrderId());

            // Get coordinates from order (handle null cases)
            LocationDTO restaurantLocation = order.getRestaurantCoordinates();
            LocationDTO customerLocation = order.getCustomerCoordinates();

            // If coordinates are null in OrderDetailsDTO, create empty LocationDTO objects
            if (restaurantLocation == null) {
                restaurantLocation = new LocationDTO(0.0, 0.0);
            }

            if (customerLocation == null) {
                customerLocation = new LocationDTO(0.0, 0.0);
            }

            // Build notification object
            return OrderAssignmentNotification.builder()
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .payment(order.getTotal() != null ? order.getTotal().toString() : "0")
                    // Restaurant information
                    .restaurantName(order.getRestaurantName())
                    .restaurantAddress(order.getPickupAddress())
                    // Customer information
                    .customerAddress(order.getDeliveryAddress())
                    // Coordinates as LocationDTO objects
                    .restaurantCoordinates(restaurantLocation)
                    .customerCoordinates(customerLocation)
                    // Special instructions
                    .specialInstructions(order.getSpecialInstructions())
                    // Assignment expiry
                    .expiryTime(assignment.getExpiryTime())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }).collect(Collectors.toList());
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