package com.order_service.order_service.service;

import com.order_service.order_service.model.CustomerLocation;
import com.order_service.order_service.repository.CustomerLocationRepository;
import com.order_service.order_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerLocationService {

    private final CustomerLocationRepository locationRepository;
    private final JwtUtil jwtUtil;

    public CustomerLocation saveOrUpdateLocation(String token, double latitude, double longitude) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        CustomerLocation location = locationRepository.findByUserId(userId)
                .orElse(new CustomerLocation());

        location.setUserId(userId);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return locationRepository.save(location);
    }

    public CustomerLocation getLocationByUser(String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        return locationRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Location not set for user."));
    }
}

