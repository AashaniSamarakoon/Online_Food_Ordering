package com.example.restaurantservice.service;

import com.example.restaurantservice.dto.MenuItemResponse;
import com.example.restaurantservice.dto.MenuRestaurantDetailsResponse;
import com.example.restaurantservice.dto.RestaurantResponse;
import com.example.restaurantservice.exception.RestaurantNotFoundException;
import com.example.restaurantservice.model.Restaurant;
import com.example.restaurantservice.repository.MenuItemRepository;
import com.example.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuRestaurantDetailsService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemService menuItemService;

    /**
     * Get restaurant details and menu items for a specific restaurant
     * @param restaurantId The ID of the restaurant
     * @return Combined restaurant and menu details
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "restaurantMenuDetails", key = "#restaurantId")
    public MenuRestaurantDetailsResponse getMenuRestaurantDetails(Long restaurantId) {
        log.info("Getting menu and restaurant details for restaurant ID: {}", restaurantId);

        // Get restaurant details
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + restaurantId));

        RestaurantResponse restaurantResponse = mapToRestaurantResponse(restaurant);

        // Get menu items for this restaurant
        List<MenuItemResponse> menuItems = menuItemService.getMenuItemsByRestaurant(restaurantId);

        // Build response
        return MenuRestaurantDetailsResponse.builder()
                .restaurantDetails(restaurantResponse)
                .menuItems(menuItems)
                .build();
    }

    /**
     * Get restaurant details and menu items for the authenticated user's restaurant
     * @return Combined restaurant and menu details
     */
    @Transactional(readOnly = true)
    public MenuRestaurantDetailsResponse getMyRestaurantDetails() {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Getting menu and restaurant details for authenticated user: {}", username);

        // Get restaurant by owner username
        Restaurant restaurant = restaurantRepository.findByOwnerUsername(username)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found for user: " + username));

        RestaurantResponse restaurantResponse = mapToRestaurantResponse(restaurant);

        // Get menu items for this restaurant
        List<MenuItemResponse> menuItems = menuItemService.getMenuItemsByRestaurant(restaurant.getId());

        // Build response
        return MenuRestaurantDetailsResponse.builder()
                .restaurantDetails(restaurantResponse)
                .menuItems(menuItems)
                .build();
    }

    /**
     * Get all restaurants with their menu items (paginated)
     * @param page Page number
     * @param size Page size
     * @return List of restaurants with their menus
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "allRestaurantsWithMenus", key = "#page + '-' + #size")
    public List<MenuRestaurantDetailsResponse> getAllRestaurantsWithMenus(int page, int size) {
        log.info("Getting all restaurants with menus, page: {}, size: {}", page, size);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Restaurant> restaurantsPage = restaurantRepository.findAll(pageRequest);

        return restaurantsPage.getContent().stream()
                .map(restaurant -> {
                    RestaurantResponse restaurantResponse = mapToRestaurantResponse(restaurant);
                    List<MenuItemResponse> menuItems = menuItemService.getMenuItemsByRestaurant(restaurant.getId());
                    return MenuRestaurantDetailsResponse.builder()
                            .restaurantDetails(restaurantResponse)
                            .menuItems(menuItems)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get only active restaurants with their menu items (paginated)
     * @param page Page number
     * @param size Page size
     * @return List of active restaurants with their menus
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "activeRestaurantsWithMenus", key = "#page + '-' + #size")
    public List<MenuRestaurantDetailsResponse> getActiveRestaurantsWithMenus(int page, int size) {
        log.info("Getting active restaurants with menus, page: {}, size: {}", page, size);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Restaurant> restaurantsPage = restaurantRepository.findByIsActiveTrue(pageRequest);

        return restaurantsPage.getContent().stream()
                .map(restaurant -> {
                    RestaurantResponse restaurantResponse = mapToRestaurantResponse(restaurant);
                    List<MenuItemResponse> menuItems = menuItemService.getMenuItemsByRestaurant(restaurant.getId());
                    return MenuRestaurantDetailsResponse.builder()
                            .restaurantDetails(restaurantResponse)
                            .menuItems(menuItems)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Search restaurants by name or address and include their menu details
     * @param query The search query
     * @param page Page number
     * @param size Page size
     * @return List of restaurants matching the search query with their menus
     */
    @Transactional(readOnly = true)
    public List<MenuRestaurantDetailsResponse> searchRestaurantsWithMenus(String query, int page, int size) {
        log.info("Searching restaurants with query: '{}', page: {}, size: {}", query, page, size);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Restaurant> restaurantsPage = restaurantRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                query, query, pageRequest);

        return restaurantsPage.getContent().stream()
                .map(restaurant -> {
                    RestaurantResponse restaurantResponse = mapToRestaurantResponse(restaurant);
                    List<MenuItemResponse> menuItems = menuItemService.getMenuItemsByRestaurant(restaurant.getId());
                    return MenuRestaurantDetailsResponse.builder()
                            .restaurantDetails(restaurantResponse)
                            .menuItems(menuItems)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Find nearby restaurants based on coordinates
     * @param latitude Current latitude
     * @param longitude Current longitude
     * @param radiusInKm Radius in kilometers
     * @param page Page number
     * @param size Page size
     * @return List of nearby restaurants with their menus
     */
    @Transactional(readOnly = true)
    public List<MenuRestaurantDetailsResponse> findNearbyRestaurantsWithMenus(
            Double latitude, Double longitude, Double radiusInKm, int page, int size) {
        log.info("Finding restaurants near ({}, {}) within {}km, page: {}, size: {}",
                latitude, longitude, radiusInKm, page, size);

        // Calculate rough bounding box for initial filtering
        double latDelta = radiusInKm / 111.0; // Approx 111km per degree of latitude
        double lonDelta = radiusInKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        double minLat = latitude - latDelta;
        double maxLat = latitude + latDelta;
        double minLon = longitude - lonDelta;
        double maxLon = longitude + lonDelta;

        // Find restaurants within bounding box (this requires a custom repository method)
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Restaurant> nearbyRestaurants = restaurantRepository
                .findByLatitudeBetweenAndLongitudeBetweenAndIsActiveTrue(
                        minLat, maxLat, minLon, maxLon, pageRequest);

        // Further filter by exact distance and map to response
        return nearbyRestaurants.stream()
                .filter(restaurant -> calculateDistance(
                        latitude, longitude, restaurant.getLatitude(), restaurant.getLongitude()) <= radiusInKm)
                .map(restaurant -> {
                    RestaurantResponse restaurantResponse = mapToRestaurantResponse(restaurant);
                    List<MenuItemResponse> menuItems = menuItemService.getMenuItemsByRestaurant(restaurant.getId());
                    return MenuRestaurantDetailsResponse.builder()
                            .restaurantDetails(restaurantResponse)
                            .menuItems(menuItems)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculate distance between two points using Haversine formula
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Map Restaurant entity to RestaurantResponse DTO
     */
    private RestaurantResponse mapToRestaurantResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .isActive(restaurant.isActive())
                .openingHours(restaurant.getOpeningHours())
                .ownerUsername(restaurant.getOwnerUsername())
                .adminId(restaurant.getAdminId())
                .username(restaurant.getUsername())
                .ownerName(restaurant.getOwnerName())
                .nic(restaurant.getNic())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .bankAccountOwner(restaurant.getBankAccountOwner())
                .bankName(restaurant.getBankName())
                .branchName(restaurant.getBranchName())
                .accountNumber(restaurant.getAccountNumber())
                .build();
    }
}