package com.delivery.orderassignmentservice.client;

import com.delivery.orderassignmentservice.dto.DriverLocationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "tracking-service", url = "${tracking.service.url}")
public interface TrackingServiceClient {

    /**
     * Get nearby available drivers based on location
     *
     * @param latitude The pickup location latitude
     * @param longitude The pickup location longitude
     * @param radius Optional radius in meters (default 5000)
     * @param limit Optional max number of drivers to return (default 10)
     * @return List of nearby drivers with their location details
     */
    @GetMapping("/api/drivers/nearby")
    List<DriverLocationDTO> getNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(required = false, defaultValue = "5000") int radius,
            @RequestParam(required = false, defaultValue = "10") int limit
    );

    /**
     * Get current location of a specific driver
     */
    @GetMapping("/api/drivers/{driverId}/location")
    DriverLocationDTO getDriverLocation(@PathVariable String driverId);

    /**
     * Create a new delivery trip
     */
//    @PostMapping("/api/trips")
//    TripDTO createTrip(@RequestBody TripCreateDTO tripData);
}