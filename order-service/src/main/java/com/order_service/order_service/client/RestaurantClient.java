package com.order_service.order_service.client;

import com.order_service.order_service.dto.FoodItem;
import com.order_service.order_service.dto.RawRestaurantResponse;
import com.order_service.order_service.dto.RestaurantResponse;
import com.order_service.order_service.model.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "restaurant-service", url = "http://localhost:8081")
public interface RestaurantClient {

    @GetMapping("/api/menu-restaurant/all")
    List<RawRestaurantResponse> getAllRawRestaurants();

    @GetMapping("/api/menu-restaurant/restaurants/{restaurantId}")
    RawRestaurantResponse getRawRestaurantById(@PathVariable("restaurantId") Long id);

    @GetMapping("/items/restaurant/{restaurantId}")
    List<FoodItem> getItemsByRestaurant(@PathVariable("restaurantId") Long restaurantId);

    @PostMapping("/api/newOrder")
    void notifyNewOrder(@RequestBody Order order);

}

