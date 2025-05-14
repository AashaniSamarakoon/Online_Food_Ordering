package com.order_service.order_service.controller;

import com.order_service.order_service.dto.RestaurantResponse;
import com.order_service.order_service.service.LocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/nearby")
    public List<RestaurantResponse> getNearbyRestaurants(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5") double radiusKm
    ) {
        return locationService.getNearbyRestaurants(latitude, longitude, radiusKm);
    }
}
