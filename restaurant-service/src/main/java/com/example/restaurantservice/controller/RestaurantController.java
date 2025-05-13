package com.example.restaurantservice.controller;

import com.example.restaurantservice.dto.RestaurantRequest;
import com.example.restaurantservice.dto.RestaurantResponse;
import com.example.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Slf4j
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public RestaurantResponse createRestaurant(
            @Valid @RequestBody RestaurantRequest request,
            Authentication authentication) {
        String ownerId = authentication.getName();
        return restaurantService.createRestaurant(request, ownerId);
    }

    @GetMapping("/my-restaurant")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public RestaurantResponse getMyRestaurant(Authentication authentication) {
        String ownerId = authentication.getName();
        return restaurantService.getRestaurantByOwner(ownerId);
    }

    @GetMapping("/sync")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public RestaurantResponse syncRestaurantFromAuth(Authentication authentication) {
        String ownerId = authentication.getName();
        String token = extractToken(authentication);
        log.info("Syncing restaurant data for owner: {} with token starting with: {}...",
                ownerId, token.substring(0, Math.min(10, token.length())));
        return restaurantService.syncRestaurantFromAuth(ownerId, token);
    }

    private String extractToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            return "Bearer " + jwtAuth.getToken().getTokenValue();
        }
        throw new IllegalStateException("Expected JWT authentication");
    }

    @PutMapping("/my-restaurant")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public RestaurantResponse updateMyRestaurant(
            @Valid @RequestBody RestaurantRequest request,
            Authentication authentication) {
        String ownerId = authentication.getName();
        RestaurantResponse myRestaurant = restaurantService.getRestaurantByOwner(ownerId);
        return restaurantService.updateRestaurant(myRestaurant.getId(), request, ownerId);
    }
}