package com.order_service.order_service.service;

import com.order_service.order_service.client.RestaurantClient;
import com.order_service.order_service.dto.*;
import com.order_service.order_service.model.Coordinates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantClient restaurantClient;

    public List<RestaurantResponse> getAllRestaurants() {
        List<RawRestaurantResponse> rawList = restaurantClient.getAllRawRestaurants();
        return rawList.stream()
                .map(this::mapToRestaurantResponse)
                .collect(Collectors.toList());
    }

    public RestaurantResponse getRestaurantById(Long id) {
        RawRestaurantResponse raw = restaurantClient.getRawRestaurantById(id);
        return mapToRestaurantResponse(raw);
    }

    public RestaurantResponse mapToRestaurantResponse(RawRestaurantResponse raw) {
        RestaurantDetails details = raw.getRestaurantDetails();

        RestaurantResponse response = new RestaurantResponse();
        response.setId(details.getId());
        response.setName(details.getName());
        response.setAddress(details.getAddress());
        //response.setIsOpen(details.isActive());

        Coordinates coords = new Coordinates();
        coords.setLatitude(details.getLatitude());
        coords.setLongitude(details.getLongitude());
        response.setRestaurantCoordinates(coords);

        response.setItems(raw.getMenuItems());
        response.setImageUrl(null); // Add logic if image exists
        response.setDistance(null); // Optional, to be calculated
        response.setCategories(
                raw.getMenuItems().stream()
                        .map(FoodItemResponse::getCategory)
                        .distinct()
                        .toList()
        );

        return response;
    }
}
