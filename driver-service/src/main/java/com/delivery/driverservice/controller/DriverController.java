package com.delivery.driverservice.controller;

import com.delivery.driverservice.client.OrderAssignmentServiceClient;
import com.delivery.driverservice.dto.*;
import com.delivery.driverservice.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Slf4j
public class DriverController{

    private final DriverService driverService;
    private final OrderAssignmentServiceClient OrderAssignmentServiceClient;

    @PostMapping
    public ResponseEntity<DriverDTO> registerDriver(@RequestBody DriverRequest request) {
        log.info("Received driver registration request: {}", request);

        try {
            DriverDTO result = driverService.registerDriver(request);
            log.info("Successfully registered driver: {}", result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error registering driver: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Adding a test endpoint to check connectivity
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Driver service is working!");
    }


    @GetMapping("/{driverId}")
    public ResponseEntity<DriverDTO> getDriverDetails(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverService.getDriverDetails(driverId));
    }

    // Driver Status Management
    @PatchMapping("/{driverId}/status")
    public ResponseEntity<DriverDTO> updateDriverStatus(
            @PathVariable Long driverId,
            @Valid @RequestBody DriverStatusUpdate update
    ) {
        // Validation
        if (!driverId.equals(update.getDriverId())) {
            throw new IllegalArgumentException("Path ID and body ID mismatch");
        }

        return ResponseEntity.ok(driverService.updateDriverStatus(update));
    }

    @GetMapping("/{driverId}/current-order")
    public ResponseEntity<OrderDetailsDTO> getCurrentOrderDetails(@PathVariable Long driverId) {
        // Verify driver exists
//        driverService.verifyDriverExists(driverId);

        // Get order details from order service
        OrderDetailsDTO orderDetails = OrderAssignmentServiceClient.getDriverActiveOrderDetails(driverId);
        return ResponseEntity.ok(orderDetails);
    }

//    // Driver Verification
//    @PatchMapping("/{driverId}/verification")
//    public ResponseEntity<DriverDTO> updateDriverVerification(
//            @PathVariable Long driverId,
//            @RequestParam boolean isVerified) {
//        return ResponseEntity.ok(driverService.updateDriverVerification(driverId, isVerified));
//    }

//    @PatchMapping("/{driverId}/verification-status")
//    public ResponseEntity<DriverDTO> updateDriverVerificationStatus(
//            @PathVariable Long driverId,
//            @Valid @RequestBody DriverVerificationUpdate update) {
//        return ResponseEntity.ok(driverService.updateDriverVerificationStatus(driverId, update));
//    }

    @GetMapping("/{driverId}/verified")
    public ResponseEntity<Boolean> isDriverVerified(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverService.isDriverVerified(driverId));
    }

    // Driver Availability
    @GetMapping("/{driverId}/available")
    public ResponseEntity<Boolean> checkDriverAvailability(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverService.isDriverAvailable(driverId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<DriverDTO>> getAvailableDrivers() {
        return ResponseEntity.ok(driverService.getAvailableDrivers());
    }

    // Driver Location and Proximity
    @PutMapping("/location")
    public ResponseEntity<DriverDTO> updateDriverLocation(@Valid @RequestBody DriverLocationUpdate update) {
        return ResponseEntity.ok(driverService.updateDriverLocation(update));
    }

//    @PostMapping("/complete-order")
//    public ResponseEntity<DriverDTO> completeOrder(@Valid @RequestBody OrderCompletionRequest request) {
//        return ResponseEntity.ok(driverService.completeOrder(request));
//    }

    // Rating Management
    @PatchMapping("/{driverId}/rating")
    public ResponseEntity<DriverDTO> updateDriverRating(
            @PathVariable Long driverId,
            @RequestParam Double rating) {
        return ResponseEntity.ok(driverService.updateDriverRating(driverId, rating));
    }
}