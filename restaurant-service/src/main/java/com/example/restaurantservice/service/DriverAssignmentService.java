package com.example.restaurantservice.service;

import com.example.restaurantservice.dto.DriverAssignmentDTO;
import com.example.restaurantservice.exception.RestaurantNotFoundException;
import com.example.restaurantservice.model.AssignedDriver;
import com.example.restaurantservice.model.Restaurant;
import com.example.restaurantservice.repository.AssignedDriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverAssignmentService {
    private final AssignedDriverRepository assignedDriverRepository;
    private final RestaurantService restaurantService;

    @Transactional
    public DriverAssignmentDTO assignDriver(Long orderId, String restaurantOwnerId, DriverAssignmentDTO assignmentDetails) {
        try {
            // Verify restaurant exists and belongs to owner
            var restaurant = restaurantService.getRestaurantByOwner(restaurantOwnerId);
            if (restaurant == null) {
                throw new RestaurantNotFoundException("Restaurant not found for owner: " + restaurantOwnerId);
            }

            // Save driver assignment details
            AssignedDriver driver = AssignedDriver.builder()
                    .orderId(orderId)
                    .driverId(assignmentDetails.getDriverId())
                    .driverName(assignmentDetails.getDriverName())
                    .driverPhone(assignmentDetails.getDriverPhone())
                    .vehicleNumber(assignmentDetails.getVehicleNumber())
                    .assignmentStatus(assignmentDetails.getStatus())
                    .currentLatitude(assignmentDetails.getLatitude())
                    .currentLongitude(assignmentDetails.getLongitude())
                    .restaurant(Restaurant.builder().id(restaurant.getId()).build())
                    .isDelivered(false)
                    .build();

            driver = assignedDriverRepository.save(driver);
            log.info("Driver assignment saved for order ID: {} with driver ID: {}", orderId, driver.getDriverId());

            return DriverAssignmentDTO.builder()
                    .driverId(driver.getDriverId())
                    .orderId(driver.getOrderId())
                    .driverName(driver.getDriverName())
                    .driverPhone(driver.getDriverPhone())
                    .vehicleNumber(driver.getVehicleNumber())
                    .status(driver.getAssignmentStatus())
                    .latitude(driver.getCurrentLatitude())
                    .longitude(driver.getCurrentLongitude())
                    .message("Driver assigned successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error assigning driver for order {}: {}", orderId, e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public DriverAssignmentDTO getDriverAssignment(Long orderId) {
        AssignedDriver driver = assignedDriverRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No driver assigned for order: " + orderId));

        return DriverAssignmentDTO.builder()
                .driverId(driver.getDriverId())
                .orderId(driver.getOrderId())
                .driverName(driver.getDriverName())
                .driverPhone(driver.getDriverPhone())
                .vehicleNumber(driver.getVehicleNumber())
                .status(driver.getAssignmentStatus())
                .latitude(driver.getCurrentLatitude())
                .longitude(driver.getCurrentLongitude())
                .build();
    }

    @Transactional
    public DriverAssignmentDTO updateDriverLocation(Long orderId, Double latitude, Double longitude) {
        AssignedDriver driver = assignedDriverRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No driver assigned for order: " + orderId));

        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        driver = assignedDriverRepository.save(driver);

        return DriverAssignmentDTO.builder()
                .driverId(driver.getDriverId())
                .orderId(driver.getOrderId())
                .status(driver.getAssignmentStatus())
                .latitude(driver.getCurrentLatitude())
                .longitude(driver.getCurrentLongitude())
                .build();
    }

    @Transactional
    public void markDeliveryComplete(Long orderId) {
        AssignedDriver driver = assignedDriverRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No driver assigned for order: " + orderId));

        driver.setDelivered(true);
        driver.setAssignmentStatus("DELIVERED");
        assignedDriverRepository.save(driver);
    }
}
