package com.example.restaurantservice.service;

import com.example.restaurantservice.dto.RestaurantRequest;
import com.example.restaurantservice.dto.RestaurantResponse;
import com.example.restaurantservice.exception.RestaurantNotFoundException;
import com.example.restaurantservice.model.Restaurant;
import com.example.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request, String adminId) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .openingHours(request.getOpeningHours())
                .isActive(true)
                .adminId(adminId)
                .build();

        restaurant = restaurantRepository.save(restaurant);
        return mapToResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
        return mapToResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));

        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setEmail(request.getEmail());
        restaurant.setOpeningHours(request.getOpeningHours());

        restaurant = restaurantRepository.save(restaurant);
        return mapToResponse(restaurant);
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    @Transactional
    public RestaurantResponse setRestaurantAvailability(Long id, Boolean isActive) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));

        restaurant.setIsActive(isActive);
        restaurant = restaurantRepository.save(restaurant);
        return mapToResponse(restaurant);
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .isActive(restaurant.getIsActive())
                .openingHours(restaurant.getOpeningHours())
                .adminId(restaurant.getAdminId())
                .build();
    }
}