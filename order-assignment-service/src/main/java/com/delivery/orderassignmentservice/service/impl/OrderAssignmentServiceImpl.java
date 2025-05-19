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
import java.util.*;
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
      // 1. Fetch order details with increased timeout
      OrderDetailsDTO order;
      try {
          log.info("Fetching order details for order ID: {}", orderId);
          order = orderServiceClient.getOrderDetails(orderId);
      } catch (Exception e) {
          log.error("Error fetching order details: {}", e.getMessage(), e);
          throw new OrderNotFoundException("Order " + orderId + " not found");
      }

      if (order == null) {
          throw new OrderNotFoundException("Order " + orderId + " not found");
      }

      // Check for null restaurant coordinates
      if (order.getRestaurantCoordinates() == null) {
          log.error("Restaurant coordinates are null for order ID: {}", orderId);
          throw new IllegalStateException("Restaurant coordinates not available for order " + orderId);
      }

      // 2. Find nearby drivers with dynamic radius
      int radius = calculateSearchRadius(orderId);
      List<DriverLocationDTO> nearbyDrivers;
      try {
          nearbyDrivers = trackingServiceClient.getNearbyDrivers(
                  order.getRestaurantCoordinates().getLatitude(),
                  order.getRestaurantCoordinates().getLongitude(),
                  radius
          );
      } catch (Exception e) {
          log.error("Error finding nearby drivers: {}", e.getMessage(), e);
          throw new NoAvailableDriversException("Error finding drivers: " + e.getMessage());
      }

      if (nearbyDrivers == null || nearbyDrivers.isEmpty()) {
          throw new NoAvailableDriversException("No drivers available within " + radius + "m");
      }

      // 3. Create a list of drivers with their details and locations
      List<DriverWithDetails> driversWithDetails = new ArrayList<>();
      for (DriverLocationDTO location : nearbyDrivers) {
          try {
              // Get driver details including rating
              DriverDTO driverDetails = driverServiceClient.getDriverDetails(location.getDriverId());
              if (driverDetails != null) {
                  driversWithDetails.add(new DriverWithDetails(location, driverDetails));
              } else {
                  log.warn("Driver details not found for driver ID: {}", location.getDriverId());
              }
          } catch (Exception e) {
              log.warn("Error fetching driver details for driver {}: {}",
                      location.getDriverId(), e.getMessage());
              // Continue with next driver instead of failing entire process
          }
      }

      if (driversWithDetails.isEmpty()) {
          throw new NoAvailableDriversException("Could not retrieve details for any drivers");
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
    public void confirmAssignment(Long orderId, Long driverId) {
        log.info("Confirming assignment for order {} by driver {}", orderId, driverId);

        try {
            // Try to find a direct assignment first
            Optional<OrderAssignment> directAssignmentOpt = assignmentRepository.findByOrderIdAndDriverId(orderId, driverId);
            OrderAssignment assignment = null;

            if (directAssignmentOpt.isPresent()) {
                // Direct assignment exists
                assignment = directAssignmentOpt.get();

                if (!"PENDING".equals(assignment.getStatus())) {
                    throw new IllegalStateException("Assignment is not in PENDING state");
                }
            } else {
                // Check if there's an assignment for this order with this driver as a candidate
                Optional<OrderAssignment> orderAssignmentOpt = assignmentRepository.findByOrderIdAndStatus(orderId, "PENDING");

                if (orderAssignmentOpt.isPresent()) {
                    OrderAssignment pendingAssignment = orderAssignmentOpt.get();

                    // Check if this driver is in candidates
                    if (pendingAssignment.getCandidateDrivers() != null &&
                            pendingAssignment.getCandidateDrivers().contains(driverId)) {
                        assignment = pendingAssignment;

                        // Set the driver ID for this assignment
                        assignment.setDriverId(driverId);
                        assignmentRepository.save(assignment);

                        // Remove from candidates since this driver is now the assigned driver
                        assignment.getCandidateDrivers().remove(driverId);
                    } else {
                        throw new AssignmentNotFoundException(
                                "No pending assignment found for order " + orderId + " and driver " + driverId);
                    }
                } else {
                    throw new AssignmentNotFoundException(
                            "No pending assignment found for order " + orderId + " and driver " + driverId);
                }
            }

            // Update assignment status
            assignment.setStatus("CONFIRMED");
            assignment.setUpdatedAt(LocalDateTime.now());
            assignmentRepository.save(assignment);

            // Update order status
            OrderStatusUpdate orderStatusUpdate = new OrderStatusUpdate();
            orderStatusUpdate.setStatus("DRIVER_CONFIRMED");
            orderStatusUpdate.setDriverId(driverId);

            try {
                orderServiceClient.updateOrderStatus(orderId, orderStatusUpdate);
            } catch (Exception e) {
                log.error("Failed to update order status: {}", e.getMessage(), e);
            }

            // Update driver status
            DriverStatusUpdate statusUpdate = new DriverStatusUpdate();
            statusUpdate.setDriverId(driverId);
            statusUpdate.setStatus("BUSY");
            statusUpdate.setLastActiveAt(LocalDateTime.now());

            try {
                driverServiceClient.updateDriverStatus(driverId, statusUpdate);
            } catch (Exception e) {
                log.error("Failed to update driver status: {}", e.getMessage(), e);
            }

            // Cancel other pending assignments for this order
            List<OrderAssignment> pendingAssignments = assignmentRepository.findByOrderIdAndStatusAndIdNot(
                    orderId, "PENDING", assignment.getId());

            for (OrderAssignment pendingAssignment : pendingAssignments) {
                pendingAssignment.setStatus("CANCELLED");
                pendingAssignment.setUpdatedAt(LocalDateTime.now());
                assignmentRepository.save(pendingAssignment);
            }

            // Send completion event
            try {
                DriverDTO driver = driverServiceClient.getDriverDetails(driverId);
                OrderDetailsDTO order = orderServiceClient.getOrderDetails(orderId);
                sendAssignmentCompletedEvent(assignment, driver, order);
            } catch (Exception e) {
                log.error("Failed to send assignment completed event: {}", e.getMessage(), e);
            }

        } catch (AssignmentNotFoundException e) {
            log.warn(e.getMessage());
            throw e; // Rethrow to maintain the 404 status
        } catch (IllegalStateException e) {
            log.warn("Invalid assignment state: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error confirming assignment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to confirm assignment", e);
        }
    }


    @Transactional
    @Override
    public void handleRejection(Long orderId, Long driverId) {
        log.info("Handling rejection for order {} by driver {}", orderId, driverId);

        try {
            // Mark assignment as rejected
            Optional<OrderAssignment> assignmentOpt = assignmentRepository.findByOrderIdAndDriverId(orderId, driverId);
            if (assignmentOpt.isEmpty()) {
                log.warn("Assignment not found for rejection - order {} driver {}", orderId, driverId);
                return; // Return silently instead of throwing an exception
            }
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
            }
        } catch (Exception e) {
            // Log exception but don't throw it further
            log.error("Error handling rejection for order {} driver {}: {}",
                    orderId, driverId, e.getMessage(), e);
        }


    }

    private void notifySingleDriver(Long orderId, Long driverId, OrderDetailsDTO order) {
//        DriverLocationDTO driver = trackingServiceClient.getDriverLocation(driverId);
        DriverAssignmentEvent event = new DriverAssignmentEvent(
                orderId,
                driverId,
                new LocationDTO(order.getRestaurantCoordinates().getLatitude(), order.getRestaurantCoordinates().getLongitude()),
//                driver.getDistance(),
//                driver.getHeading(),
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

            // Format payment amount
            String paymentAmount = order.getTotalPrice() != null
                    ? order.getTotalPrice().toString()
                    : "0.00";

            // Build notification object with updated field names
            return OrderAssignmentNotification.builder()
                    .orderId(order.getId())
                    .orderNumber("ORD-" + order.getId())  // Generate an order number
                    .payment(paymentAmount)
                    .currency("LKR")
                    // Restaurant details
                    .restaurantName(order.getRestaurantName())
                    .pickupAddress(order.getRestaurantAddress())
                    // Customer details
                    .deliveryAddress(order.getAddress())
                    .customerName(order.getUsername())
                    .phoneNumber(order.getPhoneNumber())
                    // Coordinates as LocationDTO objects
                    .restaurantCoordinates(restaurantLocation)
                    .customerCoordinates(customerLocation)
                    // Additional order details
                    .deliveryFee(order.getDeliveryCharges())
                    .specialInstructions("")  // No special instructions in the current model
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