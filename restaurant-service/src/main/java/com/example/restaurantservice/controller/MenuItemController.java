package com.example.restaurantservice.controller;

import com.example.restaurantservice.dto.MenuItemRequest;
import com.example.restaurantservice.dto.MenuItemResponse;
import com.example.restaurantservice.dto.RestaurantResponse;
import com.example.restaurantservice.exception.RestaurantNotFoundException;
import com.example.restaurantservice.service.MenuItemService;
import com.example.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {
    private final MenuItemService menuItemService;
    private final RestaurantService restaurantService;

    /**
     * Gets restaurant information, attempting to sync from Auth Service if not found locally
     */
    private RestaurantResponse getRestaurantForCurrentUser(String ownerId) {
        try {
            return restaurantService.getRestaurantByOwner(ownerId);
        } catch (RestaurantNotFoundException ex) {
            log.info("Restaurant not found for owner: {}. Trying to sync from Auth Service", ownerId);
            // This will now attempt to fetch from Auth Service
            return restaurantService.getRestaurantByOwner(ownerId);
        }
    }

    @GetMapping("/my-restaurant")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public List<MenuItemResponse> getMyMenuItems(Authentication authentication) {
        String ownerId = authentication.getName();
        RestaurantResponse restaurant = getRestaurantForCurrentUser(ownerId);
        return menuItemService.getMenuItemsByRestaurant(restaurant.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public MenuItemResponse createMenuItem(
            @Valid @RequestBody MenuItemRequest request,
            Authentication authentication) {
        try {
            String ownerId = authentication.getName();
            RestaurantResponse restaurant = getRestaurantForCurrentUser(ownerId);

            // Log the request and restaurant ID for debugging
            log.info("Creating menu item: {} for restaurant ID: {}", request, restaurant.getId());

            return menuItemService.createMenuItem(restaurant.getId(), request);
        } catch (Exception e) {
            // Improved error logging
            log.error("Error creating menu item: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/restaurant/{restaurantId}")
    public Page<MenuItemResponse> getMenuItemsByRestaurant(
            @PathVariable Long restaurantId,
            Pageable pageable) {
        return menuItemService.getAllMenuItems(restaurantId, pageable);
    }

    @GetMapping("/{id}")
    public MenuItemResponse getMenuItem(
            @PathVariable Long id,
            @RequestParam Long restaurantId) {
        return menuItemService.getMenuItem(restaurantId, id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public MenuItemResponse updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request,
            Authentication authentication) {
        String ownerId = authentication.getName();
        RestaurantResponse restaurant = getRestaurantForCurrentUser(ownerId);
        return menuItemService.updateMenuItem(restaurant.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public void deleteMenuItem(
            @PathVariable Long id,
            Authentication authentication) {
        String ownerId = authentication.getName();
        RestaurantResponse restaurant = getRestaurantForCurrentUser(ownerId);
        menuItemService.deleteMenuItem(restaurant.getId(), id);
    }

    @GetMapping("/available")
    public Page<MenuItemResponse> getAvailableMenuItems(
            @RequestParam Long restaurantId,
            Pageable pageable) {
        return menuItemService.getAvailableMenuItems(restaurantId, pageable);
    }

    @GetMapping("/category")
    public Page<MenuItemResponse> getMenuItemsByCategory(
            @RequestParam Long restaurantId,
            @RequestParam String category,
            Pageable pageable) {
        return menuItemService.getMenuItemsByCategory(restaurantId, category, pageable);
    }

    @GetMapping("/search")
    public Page<MenuItemResponse> searchMenuItems(
            @RequestParam Long restaurantId,
            @RequestParam String query,
            Pageable pageable) {
        return menuItemService.searchMenuItems(restaurantId, query, pageable);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public MenuItemResponse updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {
        String ownerId = authentication.getName();
        RestaurantResponse restaurant = getRestaurantForCurrentUser(ownerId);
        return menuItemService.updateStatus(restaurant.getId(), id, status);
    }
}