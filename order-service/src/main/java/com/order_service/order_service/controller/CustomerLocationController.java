package com.order_service.order_service.controller;

import com.order_service.order_service.model.CustomerLocation;
import com.order_service.order_service.service.CustomerLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class CustomerLocationController {

    private final CustomerLocationService locationService;

    @PostMapping("/set")
    public CustomerLocation setLocation(@RequestHeader("Authorization") String token,
                                        @RequestBody CustomerLocation location) {
        return locationService.saveOrUpdateLocation(token, location.getLatitude(), location.getLongitude());
    }

    @GetMapping("/get")
    public CustomerLocation getLocation(@RequestHeader("Authorization") String token) {
        return locationService.getLocationByUser(token);
    }
}

