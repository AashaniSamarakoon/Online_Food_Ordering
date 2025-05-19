package com.example.restaurantservice.security;

import com.example.restaurantservice.model.Restaurant;
import com.example.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantAuthorizationService {

    private final RestaurantRepository restaurantRepository;

    /**
     * Checks if the authenticated user (via JWT) is the owner of the restaurantId
     */
    public boolean isRestaurantOwner(Authentication authentication, Long restaurantId) {
        String email = authentication.getName(); // JWT "sub" claim
        return restaurantRepository.findById(restaurantId)
                .map(restaurant -> restaurant.getEmail().equals(email))
                .orElse(false);
    }
}