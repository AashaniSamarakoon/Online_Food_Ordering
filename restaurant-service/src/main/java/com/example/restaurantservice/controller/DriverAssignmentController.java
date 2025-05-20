package com.example.restaurantservice.controller;

import com.example.restaurantservice.dto.DriverAssignmentDTO;
import com.example.restaurantservice.service.DriverAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver-assignments")
@RequiredArgsConstructor
@Slf4j
public class DriverAssignmentController {
    private final DriverAssignmentService driverAssignmentService;

    @PutMapping("/{orderId}/assign")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public ResponseEntity<DriverAssignmentDTO> assignDriver(
            @PathVariable Long orderId,
            @RequestBody DriverAssignmentDTO driverDetails,
            Authentication authentication) {
        String restaurantOwnerId = authentication.getName();
        log.info("Restaurant owner {} requesting driver assignment for order {}", restaurantOwnerId, orderId);
        
        DriverAssignmentDTO response = driverAssignmentService.assignDriver(orderId, restaurantOwnerId, driverDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public ResponseEntity<DriverAssignmentDTO> getDriverAssignment(
            @PathVariable Long orderId) {
        DriverAssignmentDTO response = driverAssignmentService.getDriverAssignment(orderId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/location")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public ResponseEntity<DriverAssignmentDTO> updateDriverLocation(
            @PathVariable Long orderId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        DriverAssignmentDTO response = driverAssignmentService.updateDriverLocation(orderId, latitude, longitude);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/complete")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public ResponseEntity<Void> markDeliveryComplete(
            @PathVariable Long orderId) {
        driverAssignmentService.markDeliveryComplete(orderId);
        return ResponseEntity.ok().build();
    }
}
