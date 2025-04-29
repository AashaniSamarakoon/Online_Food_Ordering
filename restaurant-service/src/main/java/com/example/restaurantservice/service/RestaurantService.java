package com.example.restaurantservice.service;

import com.example.restaurantservice.dto.RestaurantRequest;
import com.example.restaurantservice.dto.RestaurantResponse;
import com.example.restaurantservice.exception.RestaurantNotFoundException;
import com.example.restaurantservice.exception.UnauthorizedAccessException;
import com.example.restaurantservice.model.Restaurant;
import com.example.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
//    private final PasswordEncoder passwordEncoder;

    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantResponse createRestaurant(RestaurantRequest request, String ownerUsername) {
        log.info("Creating restaurant for owner: {}", ownerUsername);

        Restaurant restaurant = Restaurant.builder()
                .username(request.getUsername())
//                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .openingHours(request.getOpeningHours())
                .isActive(true)
                .ownerUsername(ownerUsername)
                .build();

        restaurant = restaurantRepository.save(restaurant);
        log.info("Created restaurant with ID: {}", restaurant.getId());
        return mapToResponse(restaurant);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "restaurant", key = "#id")
    public RestaurantResponse getRestaurantById(Long id) {
        log.debug("Fetching restaurant by ID: {}", id);
        return restaurantRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "restaurantByOwner", key = "#username")
    public Optional<Restaurant> getRestaurantByOwnerUsername(String username) {
        log.debug("Fetching restaurant for owner: {}", username);
        return restaurantRepository.findByOwnerUsername(username);
    }
    @Transactional(readOnly = true)
    public Optional<Long> getRestaurantIdByOwnerUsername(String username) {
        return restaurantRepository.findByOwnerUsername(username)
                .map(Restaurant::getId);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "restaurants")
    public Page<RestaurantResponse> getAllRestaurants(Pageable pageable) {
        log.debug("Fetching all restaurants - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return restaurantRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    @CacheEvict(value = {"restaurant", "restaurantByOwner"}, key = "#id")
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        log.info("Updating restaurant with ID: {}", id);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));

        restaurant.updateDetails(
                request.getName(),
                request.getAddress(),
                request.getPhone(),
                request.getEmail(),
                request.getOpeningHours()
        );
//
//        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
//            restaurant.setPassword(passwordEncoder.encode(request.getPassword()));
//        }

        restaurant = restaurantRepository.save(restaurant);
        log.info("Updated restaurant with ID: {}", id);
        return mapToResponse(restaurant);
    }

    @Transactional
    @CacheEvict(value = {"restaurant", "restaurantByOwner", "restaurants"}, allEntries = true)
    public void deleteRestaurant(Long id) {
        log.info("Deleting restaurant with ID: {}", id);
        if (!restaurantRepository.existsById(id)) {
            throw new RestaurantNotFoundException(id);
        }
        restaurantRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = {"restaurant", "restaurantByOwner"}, key = "#id")
    public RestaurantResponse setRestaurantAvailability(Long id, Boolean isActive) {
        log.info("Setting availability for restaurant ID: {} to {}", id, isActive);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));

        restaurant.setActive(isActive);
        restaurant = restaurantRepository.save(restaurant);
        return mapToResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public void verifyOwnership(Long restaurantId, String username) {
        if (!restaurantRepository.existsByIdAndOwnerUsername(restaurantId, username)) {
            throw new UnauthorizedAccessException("User does not own this restaurant");
        }
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .username(restaurant.getUsername())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .isActive(restaurant.isActive())
                .openingHours(restaurant.getOpeningHours())
                .ownerUsername(restaurant.getOwnerUsername())
                .build();
    }
}