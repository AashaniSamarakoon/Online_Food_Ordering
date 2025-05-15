package com.example.restaurantauth.client;


import com.example.restaurantauth.dto.RegisterRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "restaurant-service", url = "${restaurant-service.url}")
public interface RestaurantServiceClient {


    // Update an existing restaurant
    @PutMapping("/restaurants/{id}")
    RegisterRequest updateRestaurant(
            @PathVariable("id") Long id,
            @RequestBody RegisterRequest restaurantDTO);

}