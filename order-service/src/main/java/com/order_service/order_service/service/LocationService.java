package com.order_service.order_service.service;

import com.order_service.order_service.client.RestaurantClient;
import com.order_service.order_service.dto.RestaurantResponse;
import com.order_service.order_service.model.Coordinates;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final RestaurantClient restaurantClient;

    public LocationService(RestaurantClient restaurantClient) {
        this.restaurantClient = restaurantClient;
    }

    public List<RestaurantResponse> getNearbyRestaurants(double userLat, double userLng, double radiusKm) {
        List<RestaurantResponse> allRestaurants = restaurantClient.getAllRestaurants();

        return allRestaurants.stream()
                .filter(RestaurantResponse::isOpen)
                .map(r -> {
                    Coordinates coords = r.getRestaurantCoordinates(); // Nested coordinates
                    double distance = calculateDistance(userLat, userLng, coords.getLatitude(), coords.getLongitude());
                    r.setDistance(distance);
                    return r;
                })
                .filter(r -> r.getDistance() <= radiusKm)
                .collect(Collectors.toList());
    }

    // Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
