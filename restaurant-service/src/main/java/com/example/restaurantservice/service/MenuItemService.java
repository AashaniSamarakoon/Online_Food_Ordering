package com.example.restaurantservice.service;

import com.example.restaurantservice.dto.MenuItemRequest;
import com.example.restaurantservice.dto.MenuItemResponse;
import com.example.restaurantservice.exception.MenuItemNotFoundException;
import com.example.restaurantservice.exception.RestaurantNotFoundException;
import com.example.restaurantservice.model.MenuItem;
import com.example.restaurantservice.model.Restaurant;
import com.example.restaurantservice.repository.MenuItemRepository;
import com.example.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    @CacheEvict(value = "menuItems", key = "#restaurantId")
    public MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request) {
        log.info("Creating menu item for restaurant ID: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        // Create the menu item
        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setCategory(request.getCategory());
        menuItem.setPrice(request.getPrice());
        menuItem.setStatus(MenuItem.ItemStatus.valueOf(request.getStatus()));
        menuItem.setDescription(request.getDescription());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setRestaurant(restaurant);  // Set the restaurant!

        // Log the entity before saving
        log.info("Saving menu item: {}", menuItem);

        // Save the menu item
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        // Return the response
        return mapToMenuItemResponse(savedMenuItem);
    }

    private MenuItemResponse mapToMenuItemResponse(MenuItem menuItem) {
        // Implement this method to convert MenuItem entity to MenuItemResponse DTO
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .category(menuItem.getCategory())
                .price(menuItem.getPrice())
                .status(menuItem.getStatus().name())
                .description(menuItem.getDescription())
                .imageUrl(menuItem.getImageUrl())
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "menuItems", key = "#restaurantId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<MenuItemResponse> getAllMenuItems(Long restaurantId, Pageable pageable) {
        log.debug("Fetching paginated menu items for restaurant ID: {}", restaurantId);
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        return menuItemRepository.findByRestaurantId(restaurantId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "menuItemsList", key = "#restaurantId")
    public List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId) {
        log.debug("Fetching all menu items for restaurant ID: {}", restaurantId);
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItem(Long restaurantId, Long menuItemId) {
        log.debug("Fetching menu item ID: {} for restaurant ID: {}", menuItemId, restaurantId);
        return menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));
    }

    @Transactional
    @CacheEvict(value = {"menuItems", "menuItemsList"}, key = "#restaurantId")
    public MenuItemResponse updateMenuItem(Long restaurantId, Long menuItemId, MenuItemRequest request) {
        log.info("Updating menu item ID: {} for restaurant ID: {}", menuItemId, restaurantId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));

        menuItem.setName(request.getName());
        menuItem.setCategory(request.getCategory());
        menuItem.setPrice(request.getPrice());
        menuItem.setStatus(MenuItem.ItemStatus.valueOf(request.getStatus().toUpperCase()));
        menuItem.setDescription(request.getDescription());

        if (request.getImageUrl() != null) {
            menuItem.setImageUrl(request.getImageUrl());
        }

        menuItem = menuItemRepository.save(menuItem);
        log.info("Updated menu item ID: {} for restaurant ID: {}", menuItemId, restaurantId);
        return mapToResponse(menuItem);
    }

    @Transactional
    @CacheEvict(value = {"menuItems", "menuItemsList"}, key = "#restaurantId")
    public void deleteMenuItem(Long restaurantId, Long menuItemId) {
        log.info("Deleting menu item ID: {} from restaurant ID: {}", menuItemId, restaurantId);

        if (!menuItemRepository.existsByIdAndRestaurantId(menuItemId, restaurantId)) {
            throw new MenuItemNotFoundException(menuItemId);
        }
        menuItemRepository.deleteById(menuItemId);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getAvailableMenuItems(Long restaurantId, Pageable pageable) {
        log.debug("Fetching available menu items for restaurant ID: {}", restaurantId);
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        return menuItemRepository.findByRestaurantIdAndStatus(
                restaurantId,
                MenuItem.ItemStatus.AVAILABLE,
                pageable
        ).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, String category, Pageable pageable) {
        log.debug("Fetching menu items by category '{}' for restaurant ID: {}", category, restaurantId);
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        return menuItemRepository.findByRestaurantIdAndCategoryIgnoreCase(restaurantId, category, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> searchMenuItems(Long restaurantId, String query, Pageable pageable) {
        log.debug("Searching menu items with query '{}' for restaurant ID: {}", query, restaurantId);
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        return menuItemRepository.findByRestaurantIdAndNameContainingIgnoreCase(restaurantId, query, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    @CacheEvict(value = {"menuItems", "menuItemsList"}, key = "#restaurantId")
    public MenuItemResponse updateStatus(Long restaurantId, Long menuItemId, String status) {
        log.info("Updating status to '{}' for menu item ID: {} in restaurant ID: {}", status, menuItemId, restaurantId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));
        menuItem.setStatus(MenuItem.ItemStatus.valueOf(status.toUpperCase()));
        menuItem = menuItemRepository.save(menuItem);
        return mapToResponse(menuItem);
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .category(menuItem.getCategory())
                .price(menuItem.getPrice())
                .status(menuItem.getStatus().name())
                .description(menuItem.getDescription())
                .imageUrl(menuItem.getImageUrl())
                .build();
    }
}