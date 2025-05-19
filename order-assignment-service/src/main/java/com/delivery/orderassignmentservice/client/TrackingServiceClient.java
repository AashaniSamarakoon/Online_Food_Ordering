package com.delivery.orderassignmentservice.client;

import com.delivery.orderassignmentservice.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "tracking-service", url = "${tracking.service.url}")
public interface TrackingServiceClient {

    /**
     * Get nearby available drivers based on location
     */
    @GetMapping("/api/tracking/drivers/nearby")
    List<DriverLocationDTO> getNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(required = false, defaultValue = "5000") int radius
    );

    /**
     * Get current location of a specific driver
     */
    @GetMapping("/api/tracking/drivers/{driverId}/location")
    DriverLocationDTO getDriverLocation(@PathVariable String driverId);

    /**
     * Create a pending trip (before driver assignment)
     */
    @PostMapping("/api/tracking/trips/pending")
    Object createPendingTrip(@RequestBody TripCreateDTO tripData);

    /**
     * Assign driver to existing trip
     */
    @PostMapping("/api/tracking/trips/{orderId}/assign")
    Object assignDriverToTrip(@PathVariable String orderId, @RequestBody DriverAssignmentDTO driverData);

    /**
     * Centralized status update endpoint
     */
    @PostMapping("/api/tracking/status/{orderId}")
    Map<String, Object> updateOrderStatus(@PathVariable String orderId, @RequestBody StatusUpdateRequest statusUpdate);
}