package com.example.restaurantservice.controller;

import com.example.restaurantservice.dto.RestaurantRequest;
import com.example.restaurantservice.dto.RestaurantResponse;
import com.example.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse createRestaurant(
            @Valid @RequestBody RestaurantRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String adminId = jwt.getSubject();
        return restaurantService.createRestaurant(request, adminId);
    }

    @GetMapping("/{id}")
    public RestaurantResponse getRestaurant(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id);
    }

    @GetMapping
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    @PutMapping("/{id}")
    public RestaurantResponse updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {
        return restaurantService.updateRestaurant(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
    }

    @PatchMapping("/{id}/availability")
    public RestaurantResponse setRestaurantAvailability(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        return restaurantService.setRestaurantAvailability(id, isActive);
    }
}