package com.order_service.order_service.service;

import com.order_service.order_service.client.RestaurantClient;
import com.order_service.order_service.dto.RestaurantResponse;
import com.order_service.order_service.model.CustomerLocation;
import com.order_service.order_service.util.GeoUtil;
import com.order_service.order_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final JwtUtil jwtUtil;
    private final CustomerLocationService locationService;
    private final RestaurantClient restaurantClient;
    private final RestaurantService restaurantService;

    public double calculateDeliveryFee(String token, Long restaurantId) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        CustomerLocation userLocation = locationService.getLocationByUser(token);

        RestaurantResponse restaurant = restaurantService.getRestaurantById(restaurantId);
        double restaurantLat = restaurant.getRestaurantCoordinates().getLatitude();
        double restaurantLon = restaurant.getRestaurantCoordinates().getLongitude();

        double distance = GeoUtil.calculateDistance(
                userLocation.getLatitude(), userLocation.getLongitude(),
                restaurantLat, restaurantLon
        );

        return Math.round(distance * 100.0);
    }
}

