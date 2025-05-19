package com.example.restaurantservice.client;

import com.example.restaurantservice.config.FeignClientConfig;
import com.example.restaurantservice.dto.RestaurantRegistrationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "restaurant-auth", url = "${restaurant-auth.url}", configuration = FeignClientConfig.class)

public interface AuthServiceClient {

    @GetMapping("/api/restaurant/by-owner/{ownerId}")
    RestaurantRegistrationResponse getRestaurantByOwner(
            @PathVariable("ownerId") String ownerId
    );

    @GetMapping("/api/restaurant/admins/verified")
    List<RestaurantRegistrationResponse> getAllVerifiedRestaurants(
            @RequestHeader("Authorization") String authToken
    );
}