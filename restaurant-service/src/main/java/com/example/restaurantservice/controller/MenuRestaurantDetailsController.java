package com.example.restaurantservice.controller;

import com.example.restaurantservice.dto.MenuRestaurantDetailsResponse;
import com.example.restaurantservice.service.MenuRestaurantDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu-restaurant")
@RequiredArgsConstructor
@Slf4j
public class MenuRestaurantDetailsController {

    private final MenuRestaurantDetailsService menuRestaurantDetailsService;

    /**
     * Get menu items with restaurant details for a specific restaurant
     * @param restaurantId The ID of the restaurant
     * @return MenuRestaurantDetailsResponse containing restaurant details and menu items
     */
    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<MenuRestaurantDetailsResponse> getMenuWithRestaurantDetails(
            @PathVariable Long restaurantId) {
        log.info("Fetching menu and restaurant details for restaurant ID: {}", restaurantId);
        MenuRestaurantDetailsResponse response = menuRestaurantDetailsService.getMenuRestaurantDetails(restaurantId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get menu items with restaurant details for the currently authenticated restaurant owner
     * @return MenuRestaurantDetailsResponse containing restaurant details and menu items
     */
    @GetMapping("/my-restaurant")
    public ResponseEntity<MenuRestaurantDetailsResponse> getMyRestaurantMenuDetails() {
        log.info("Fetching menu and restaurant details for authenticated user's restaurant");
        MenuRestaurantDetailsResponse response = menuRestaurantDetailsService.getMyRestaurantDetails();
        return ResponseEntity.ok(response);
    }

    /**
     * Get all restaurants with their menu items (paginated)
     * @param page Page number (default 0)
     * @param size Page size (default 10)
     * @return List of MenuRestaurantDetailsResponse
     */
    @GetMapping("/all")
    public ResponseEntity<java.util.List<MenuRestaurantDetailsResponse>> getAllRestaurantsWithMenus(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all restaurants with their menus, page: {}, size: {}", page, size);
        java.util.List<MenuRestaurantDetailsResponse> responses =
                menuRestaurantDetailsService.getAllRestaurantsWithMenus(page, size);
        return ResponseEntity.ok(responses);
    }
}