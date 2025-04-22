package com.order_service.order_service.client;

import com.order_service.order_service.dto.FoodItem;
import com.order_service.order_service.dto.Restaurant;
import com.order_service.order_service.dto.RestaurantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "restaurant-service", url = "http://localhost:8083") // change port in production
public interface RestaurantClient {

    @GetMapping("/api/restaurants")
    List<RestaurantResponse> getAllRestaurants();

    @GetMapping("/restaurants/{id}")
    RestaurantResponse getRestaurantById(@PathVariable("id") Long id);

    @GetMapping("/items/restaurant/{restaurantId}")
    List<FoodItem> getItemsByRestaurant(@PathVariable("restaurantId") Long restaurantId);
}

