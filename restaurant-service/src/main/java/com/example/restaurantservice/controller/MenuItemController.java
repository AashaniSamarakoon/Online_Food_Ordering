package com.example.restaurantservice.controller;

import com.example.restaurantservice.dto.MenuItemRequest;
import com.example.restaurantservice.dto.MenuItemResponse;
import com.example.restaurantservice.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    // Create a new menu item
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        return menuItemService.createMenuItem(restaurantId, request);
    }

    // Get all menu items for a restaurant with pagination
    @GetMapping
    public Page<MenuItemResponse> getAllMenuItems(
            @PathVariable Long restaurantId,
            Pageable pageable) {
        return menuItemService.getAllMenuItems(restaurantId, pageable);
    }

    // Get a specific menu item by ID
    @GetMapping("/{menuItemId}")
    public MenuItemResponse getMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {
        return menuItemService.getMenuItem(restaurantId, menuItemId);
    }

    // Update a specific menu item
    @PutMapping("/{menuItemId}")
    public MenuItemResponse updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @Valid @RequestBody MenuItemRequest request) {
        return menuItemService.updateMenuItem(restaurantId, menuItemId, request);
    }

    // Delete a menu item
    @DeleteMapping("/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {
        menuItemService.deleteMenuItem(restaurantId, menuItemId);
    }

    // Update availability of a menu item
    @PatchMapping("/{menuItemId}/availability")
    public MenuItemResponse updateAvailability(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @RequestParam Boolean available) {
        return menuItemService.updateAvailability(restaurantId, menuItemId, available);
    }

    // Get all available menu items (paginated)
    @GetMapping("/available")
    public Page<MenuItemResponse> getAvailableMenuItems(
            @PathVariable Long restaurantId,
            Pageable pageable) {
        return menuItemService.getAvailableMenuItems(restaurantId, pageable);
    }

    // Get menu items by category (paginated)
    @GetMapping("/category/{category}")
    public Page<MenuItemResponse> getMenuItemsByCategory(
            @PathVariable Long restaurantId,
            @PathVariable String category,
            Pageable pageable) {
        return menuItemService.getMenuItemsByCategory(restaurantId, category, pageable);
    }
}
