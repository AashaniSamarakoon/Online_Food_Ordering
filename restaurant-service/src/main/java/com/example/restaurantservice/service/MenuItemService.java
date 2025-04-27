package com.example.restaurantservice.service;

import com.example.restaurantservice.dto.MenuItemRequest;
import com.example.restaurantservice.dto.MenuItemResponse;
import com.example.restaurantservice.exception.MenuItemNotFoundException;
import com.example.restaurantservice.exception.RestaurantNotFoundException;
import com.example.restaurantservice.model.MenuItem;
import com.example.restaurantservice.model.Restaurant;
import com.example.restaurantservice.repository.MenuItemRepository;
import com.example.restaurantservice.repository.RestaurantRepository;
import com.example.restaurantservice.config.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    @CacheEvict(value = "menuItems", key = "#restaurantId")
    public MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageUrl = fileStorageService.storeFile(request.getImage());
        }

        MenuItem menuItem = MenuItem.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .status(convertToItemStatus(request.getStatus()))
                .description(request.getDescription())
                .imageUrl(imageUrl)
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

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = fileStorageService.storeFile(request.getImage());
            menuItem.setImageUrl(imageUrl);
        }

        menuItem.setName(request.getName());
        menuItem.setCategory(request.getCategory());
        menuItem.setPrice(request.getPrice());
        menuItem.setStatus(convertToItemStatus(request.getStatus()));
        menuItem.setDescription(request.getDescription());

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
        return menuItemRepository.findByRestaurantIdAndStatus(
                        restaurantId,
                        MenuItem.ItemStatus.AVAILABLE,
                        pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, String category, Pageable pageable) {
        return menuItemRepository.findByRestaurantIdAndCategoryIgnoreCase(restaurantId, category, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> searchMenuItems(Long restaurantId, String query, Pageable pageable) {
        return menuItemRepository.findByRestaurantIdAndNameContainingIgnoreCase(restaurantId, query, pageable)
                .map(this::mapToResponse);
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

    @Transactional
    @CacheEvict(value = "menuItems", key = "#restaurantId")
    public MenuItemResponse updateStatus(Long restaurantId, Long menuItemId, String status) {
        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));
        menuItem.setStatus(convertToItemStatus(status));
        menuItem = menuItemRepository.save(menuItem);
        return mapToResponse(menuItem);
    }

    private MenuItem.ItemStatus convertToItemStatus(String status) {
        try {
            return MenuItem.ItemStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            String allowedValues = Arrays.stream(MenuItem.ItemStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid status value. Allowed values are: " + allowedValues);
        }
    }
}