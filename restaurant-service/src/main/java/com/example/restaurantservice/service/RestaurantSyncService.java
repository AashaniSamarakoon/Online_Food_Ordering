package com.example.restaurantservice.service;

import com.example.restaurantservice.client.AuthServiceClient;
import com.example.restaurantservice.dto.RestaurantRegistrationResponse;
import com.example.restaurantservice.model.Restaurant;
import com.example.restaurantservice.repository.RestaurantRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantSyncService {
    private final AuthServiceClient authServiceClient;
    private final RestaurantRepository restaurantRepository;

    /**
     * Syncs all verified restaurants from the auth service
     */
    @Transactional
    public List<Restaurant> syncAllVerifiedRestaurants(String bearerToken) {
        try {
            log.info("Starting full sync of verified restaurants from auth service");
            List<RestaurantRegistrationResponse> verifiedRestaurants =
                    authServiceClient.getAllVerifiedRestaurants(bearerToken);

            log.info("Found {} verified restaurants in auth service", verifiedRestaurants.size());

            List<Restaurant> syncedRestaurants = verifiedRestaurants.stream()
                    .map(this::createOrUpdateRestaurant)
                    .collect(Collectors.toList());

            log.info("Successfully synced {} restaurants", syncedRestaurants.size());
            return syncedRestaurants;

        } catch (FeignException e) {
            log.error("Failed to sync restaurants: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Restaurant createOrUpdateRestaurant(RestaurantRegistrationResponse response) {
        return restaurantRepository.findByOwnerId(response.getEmail())
                .map(restaurant -> {
                    // Update existing restaurant
                    restaurant.setName(response.getRestaurantName());
                    restaurant.setAddress(response.getAddress());
                    restaurant.setPhone(response.getPhone());
                    restaurant.setEmail(response.getEmail());
                    restaurant.setActive(response.isVerified());
                    log.debug("Updated restaurant: {}", restaurant.getId());
                    return restaurantRepository.save(restaurant);
                })
                .orElseGet(() -> {
                    // Create new restaurant
                    Restaurant newRestaurant = Restaurant.builder()
                            .name(response.getRestaurantName())
                            .ownerId(response.getEmail())
                            .address(response.getAddress())
                            .phone(response.getPhone())
                            .email(response.getEmail())
                            .openingHours("9:00 AM - 10:00 PM") // Default value
                            .isActive(response.isVerified())
                            .build();

                    Restaurant saved = restaurantRepository.save(newRestaurant);
                    log.debug("Created new restaurant: {}", saved.getId());
                    return saved;
                });
    }
}