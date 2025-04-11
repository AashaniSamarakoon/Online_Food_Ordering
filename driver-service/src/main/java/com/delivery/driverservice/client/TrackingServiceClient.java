package com.delivery.driverservice.client;

import com.delivery.driverservice.dto.tracking.CreateTrackingDTO;
import com.delivery.driverservice.dto.tracking.LocationUpdateDTO;
import com.delivery.driverservice.dto.tracking.TrackingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "tracking-service", url = "${tracking.service.url}")
public interface TrackingServiceClient {

    @PostMapping("/api/tracking")
    TrackingDTO createTracking(@RequestBody CreateTrackingDTO dto);

    @PutMapping("/api/tracking/{trackingId}/location")
    TrackingDTO updateLocation(
            @PathVariable String trackingId,
            @RequestBody LocationUpdateDTO update);

    @GetMapping("/api/tracking/driver/{driverId}")
    TrackingDTO getActiveTrackingByDriver(@PathVariable String driverId);
}