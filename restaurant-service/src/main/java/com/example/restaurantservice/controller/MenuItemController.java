package com.example.restaurantservice.controller;

import com.example.restaurantservice.config.SecurityConstants;
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
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;
    private final RestaurantService restaurantService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    public MenuItemResponse createMenuItem(
            @PathVariable Long restaurantId,
            @ModelAttribute @Valid MenuItemRequest request,
            Authentication authentication) {

        validateRestaurantOwnership(restaurantId, authentication);
        return menuItemService.createMenuItem(restaurantId, request);
    }

    @GetMapping
    public Page<MenuItemResponse> getAllMenuItems(
            @PathVariable Long restaurantId,
            Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

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
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {
        return menuItemService.getMenuItem(restaurantId, menuItemId);
    }

    @PutMapping(value = "/{menuItemId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    public MenuItemResponse updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @ModelAttribute @Valid MenuItemRequest request,
            Authentication authentication) {

        validateRestaurantOwnership(restaurantId, authentication);
        return menuItemService.updateMenuItem(restaurantId, menuItemId, request);
    }

    @DeleteMapping("/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    public void deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            Authentication authentication) {

        validateRestaurantOwnership(restaurantId, authentication);
        menuItemService.deleteMenuItem(restaurantId, menuItemId);
    }

    @PatchMapping("/{menuItemId}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    public MenuItemResponse updateStatus(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @RequestParam String status,
            Authentication authentication) {

        validateRestaurantOwnership(restaurantId, authentication);
        return menuItemService.updateStatus(restaurantId, menuItemId, status);
    }

    @GetMapping("/available")
    public Page<MenuItemResponse> getAvailableMenuItems(
            @PathVariable Long restaurantId,
            Pageable pageable) {
        return menuItemService.getAvailableMenuItems(restaurantId, pageable);
    }

    @GetMapping("/category/{category}")
    public Page<MenuItemResponse> getMenuItemsByCategory(
            @PathVariable Long restaurantId,
            @PathVariable String category,
            Pageable pageable) {
        return menuItemService.getMenuItemsByCategory(restaurantId, category, pageable);
    }

    @GetMapping("/search")
    public Page<MenuItemResponse> searchMenuItems(
            @PathVariable Long restaurantId,
            @RequestParam String query,
            Pageable pageable) {
        return menuItemService.searchMenuItems(restaurantId, query, pageable);
    }

    private void validateRestaurantOwnership(Long restaurantId, Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

//        if (!isAdmin && !restaurantService.isRestaurantOwnedBy(restaurantId, username)) {
//            throw new UnauthorizedAccessException(
//                    "You don't have permission to modify menu items for restaurant with ID: " + restaurantId);
//        }
    }
}