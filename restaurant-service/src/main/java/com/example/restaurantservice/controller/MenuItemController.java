package com.example.restaurantservice.controller;

import com.example.restaurantservice.dto.MenuItemRequest;
import com.example.restaurantservice.dto.MenuItemResponse;
import com.example.restaurantservice.exception.UnauthorizedAccessException;
import com.example.restaurantservice.service.MenuItemService;
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
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;
    private final RestaurantService restaurantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public MenuItemResponse createMenuItem(
            @RequestBody @Valid MenuItemRequest request,
            Authentication authentication) {

        Long restaurantId = getRestaurantIdFromAuthentication(authentication);
        return menuItemService.createMenuItem(restaurantId, request);
    }

    @GetMapping
    public Page<MenuItemResponse> getAllMenuItems(
            Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            Authentication authentication) {

        Long restaurantId = getRestaurantIdFromAuthentication(authentication);

        if (category != null) {
            return menuItemService.getMenuItemsByCategory(restaurantId, category, pageable);
        }
        if (search != null) {
            return menuItemService.searchMenuItems(restaurantId, search, pageable);
        }
        return menuItemService.getAllMenuItems(restaurantId, pageable);
    }

    @GetMapping("/{menuItemId}")
    public MenuItemResponse getMenuItem(
            @PathVariable Long menuItemId,
            Authentication authentication) {

        Long restaurantId = getRestaurantIdFromAuthentication(authentication);
        return menuItemService.getMenuItem(restaurantId, menuItemId);
    }

    @PutMapping("/{menuItemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public MenuItemResponse updateMenuItem(
            @PathVariable Long menuItemId,
            @RequestBody @Valid MenuItemRequest request,
            Authentication authentication) {

        Long restaurantId = getRestaurantIdFromAuthentication(authentication);
        return menuItemService.updateMenuItem(restaurantId, menuItemId, request);
    }

    @DeleteMapping("/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public void deleteMenuItem(
            @PathVariable Long menuItemId,
            Authentication authentication) {

        Long restaurantId = getRestaurantIdFromAuthentication(authentication);
        menuItemService.deleteMenuItem(restaurantId, menuItemId);
    }

    @PatchMapping("/{menuItemId}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public MenuItemResponse updateStatus(
            @PathVariable Long menuItemId,
            @RequestParam String status,
            Authentication authentication) {

        Long restaurantId = getRestaurantIdFromAuthentication(authentication);
        return menuItemService.updateStatus(restaurantId, menuItemId, status);
    }

    @GetMapping("/available")
    public Page<MenuItemResponse> getAvailableMenuItems(
            Pageable pageable,
            Authentication authentication) {

        Long restaurantId = getRestaurantIdFromAuthentication(authentication);
        return menuItemService.getAvailableMenuItems(restaurantId, pageable);
    }

    private Long getRestaurantIdFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return restaurantService.getRestaurantIdByOwnerUsername(username)
                .orElseThrow(() -> new UnauthorizedAccessException("User doesn't own a restaurant"));
    }
}