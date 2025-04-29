package com.example.restaurantservice.controller;

import com.example.restaurantservice.dto.RestaurantRequest;
import com.example.restaurantservice.dto.RestaurantResponse;
import com.example.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public RestaurantResponse createRestaurant(
            @Valid @RequestBody RestaurantRequest request,
            Authentication authentication) {
        String ownerUsername = authentication.getName();
        return restaurantService.createRestaurant(request, ownerUsername);
    }

    @GetMapping("/{id}")
    public RestaurantResponse getRestaurant(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id);
    }

    @GetMapping
    public Page<RestaurantResponse> getAllRestaurants(Pageable pageable) {
        return restaurantService.getAllRestaurants(pageable);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public RestaurantResponse updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request,
            Authentication authentication) {
        // Verify ownership
        restaurantService.verifyOwnership(id, authentication.getName());
        return restaurantService.updateRestaurant(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    public RestaurantResponse setRestaurantAvailability(
            @PathVariable Long id,
            @RequestParam Boolean isActive,
            Authentication authentication) {
        // Verify ownership if not admin
        if (!authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            restaurantService.verifyOwnership(id, authentication.getName());
        }
        return restaurantService.setRestaurantAvailability(id, isActive);
    }
}