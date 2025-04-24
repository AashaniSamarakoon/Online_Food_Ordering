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

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    @CacheEvict(value = "menuItems", key = "#restaurantId")
    public MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        MenuItem menuItem = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .available(request.getAvailable())
                .restaurant(restaurant)
                .build();

        menuItem = menuItemRepository.save(menuItem);
        log.info("Created menu item {} for restaurant {}", menuItem.getId(), restaurantId);
        return mapToResponse(menuItem);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "menuItems", key = "#restaurantId")
    public Page<MenuItemResponse> getAllMenuItems(Long restaurantId, Pageable pageable) {
        return menuItemRepository.findByRestaurantId(restaurantId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItem(Long restaurantId, Long menuItemId) {
        return menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));
    }

    @Transactional
    @CacheEvict(value = "menuItems", key = "#restaurantId")
    public MenuItemResponse updateMenuItem(Long restaurantId, Long menuItemId, MenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setAvailable(request.getAvailable());

        menuItem = menuItemRepository.save(menuItem);
        log.info("Updated menu item {} for restaurant {}", menuItemId, restaurantId);
        return mapToResponse(menuItem);
    }

    @Transactional
    @CacheEvict(value = "menuItems", key = "#restaurantId")
    public void deleteMenuItem(Long restaurantId, Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));
        menuItemRepository.delete(menuItem);
        log.info("Deleted menu item {} from restaurant {}", menuItemId, restaurantId);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getAvailableMenuItems(Long restaurantId, Pageable pageable) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, String category, Pageable pageable) {
        return menuItemRepository.findByRestaurantIdAndCategoryIgnoreCase(restaurantId, category, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    @CacheEvict(value = "menuItems", key = "#restaurantId")
    public MenuItemResponse updateAvailability(Long restaurantId, Long menuItemId, Boolean available) {
        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));
        menuItem.setAvailable(available);
        menuItem = menuItemRepository.save(menuItem);
        return mapToResponse(menuItem);
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .category(menuItem.getCategory())
                .available(menuItem.getAvailable())
                .build();
    }
}