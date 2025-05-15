package com.example.restaurantservice.service;

import com.example.restaurantservice.client.AuthServiceClient;
import com.example.restaurantservice.dto.RestaurantRegistrationResponse;
import com.example.restaurantservice.dto.RestaurantRequest;
import com.example.restaurantservice.dto.RestaurantResponse;
import com.example.restaurantservice.exception.RestaurantAlreadyExistsException;
import com.example.restaurantservice.exception.RestaurantNotFoundException;
import com.example.restaurantservice.model.Restaurant;
import com.example.restaurantservice.repository.RestaurantRepository;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final AuthServiceClient authServiceClient;

    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantResponse createRestaurant(@Valid RestaurantRequest request, String ownerId) {
        log.info("Creating restaurant for owner: {}", ownerId);

        if (restaurantRepository.existsByOwnerId(ownerId)) {
            throw new RestaurantAlreadyExistsException("Owner already has a restaurant");
        }

        Restaurant restaurant = Restaurant.builder()
                .ownerId(ownerId)
                .adminId(ownerId)  // Set admin_id to same as owner_id
                .username(ownerId) // Set username to same as owner_id
                .ownerUsername(ownerId) // Set owner_username to same as owner_id
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .openingHours(request.getOpeningHours())
                .isActive(true)
                .password("PLACEHOLDER_NOT_USED")  // Add placeholder password
                .build();

        restaurant = restaurantRepository.save(restaurant);
        log.info("Created restaurant with ID: {}", restaurant.getId());
        return mapToResponse(restaurant);
    }


    @Transactional(readOnly = true)
    @Cacheable(value = "restaurantByOwner", key = "#ownerId")
    public RestaurantResponse getRestaurantByOwner(String ownerId) {
        log.debug("Fetching restaurant for owner: {}", ownerId);

        // First try to find restaurant in local database
        return restaurantRepository.findByOwnerId(ownerId)
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    // If not found, fetch from auth service and create it
                    try {
                        log.info("Restaurant not found locally, fetching from auth service for owner: {}", ownerId);
                        RestaurantRegistrationResponse regResponse = authServiceClient.getRestaurantByOwner(ownerId);

                        if (regResponse != null) {
                            log.info("Found restaurant in auth service: {}", regResponse.getRestaurantName());
                            // Create a new restaurant from the registration data
                            Restaurant restaurant = createRestaurantFromRegistration(regResponse);
                            restaurant = restaurantRepository.save(restaurant);
                            log.info("Created restaurant from auth service data: {}", restaurant.getId());
                            return mapToResponse(restaurant);
                        }
                    } catch (FeignException e) {
                        log.error("Error fetching restaurant from auth service: {}", e.getMessage());
                        log.debug("Detailed error: ", e);
                    }

                    throw new RestaurantNotFoundException(ownerId);
                });
    }

    private Restaurant createRestaurantFromRegistration(RestaurantRegistrationResponse regResponse) {
        log.debug("Creating restaurant from registration data: {}", regResponse);

        String email = regResponse.getEmail();

        Restaurant restaurant = Restaurant.builder()
                .name(regResponse.getRestaurantName())
                .ownerId(email)
                .adminId(email)  // Set admin_id to same as owner_id if they're the same
                .username(email) // Set username to same as email
                .ownerUsername(email) // Set owner_username to same as email
                .address(regResponse.getAddress())
                .phone(regResponse.getPhone())
                .email(email)
                .ownerName(regResponse.getOwnerName())
                .nic(regResponse.getNic())
                .latitude(regResponse.getLatitude())
                .longitude(regResponse.getLongitude())
                .bankAccountOwner(regResponse.getBankAccountOwner())
                .bankName(regResponse.getBankName())
                .branchName(regResponse.getBranchName())
                .accountNumber(regResponse.getAccountNumber())
                .openingHours("9:00 AM - 10:00 PM") // Default value
                .isActive(regResponse.isVerified()) // Set active based on verification status
                .password("PLACEHOLDER_NOT_USED")  // Add this placeholder for the required password field
                .build();

        log.debug("Created restaurant entity: {}", restaurant);
        return restaurant;
    }
    @Transactional
    @CacheEvict(value = {"restaurant", "restaurantByOwner"}, key = "#id")
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request, String ownerId) {
        log.info("Updating restaurant with ID: {} for owner: {}", id, ownerId);

        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new RestaurantNotFoundException(id));

        restaurant.updateDetails(
                request.getName(),
                request.getAddress(),
                request.getPhone(),
                request.getEmail(),
                request.getOpeningHours()
        );

        restaurant = restaurantRepository.save(restaurant);
        log.info("Updated restaurant with ID: {}", id);
        return mapToResponse(restaurant);
    }

    @Transactional
    @CacheEvict(value = {"restaurant", "restaurantByOwner", "restaurants"}, allEntries = true)
    public void deleteRestaurant(Long id, String ownerId) {
        log.info("Deleting restaurant with ID: {} for owner: {}", id, ownerId);
        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
        restaurantRepository.delete(restaurant);
    }

    @Transactional
    @CacheEvict(value = {"restaurant", "restaurantByOwner"}, key = "#id")
    public RestaurantResponse setRestaurantAvailability(Long id, Boolean isActive, String ownerId) {
        log.info("Setting availability for restaurant ID: {} to {}", id, isActive);

        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new RestaurantNotFoundException(id));

        restaurant.setActive(isActive);
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
                .isActive(restaurant.isActive())
                .openingHours(restaurant.getOpeningHours())
                .ownerUsername(restaurant.getOwnerId()) // Using ownerId as ownerUsername
                .adminId(restaurant.getAdminId())  // Map admin_id to response
                .username(restaurant.getUsername()) // Map username to response
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
    @Transactional
    public RestaurantResponse syncRestaurantFromAuth(String ownerId, String authToken) {
        log.info("Explicitly syncing restaurant from auth service for owner: {}", ownerId);

        try {
            RestaurantRegistrationResponse regResponse =
                    authServiceClient.getRestaurantByOwner(ownerId);

            if (regResponse != null) {
                log.info("Received restaurant data from auth service: {}", regResponse.getRestaurantName());

                // Check if restaurant already exists locally
                Restaurant restaurant = restaurantRepository.findByOwnerId(ownerId)
                        .map(existingRestaurant -> {
                            // Update existing restaurant with fresh data
                            updateRestaurantFromAuth(existingRestaurant, regResponse);
                            log.info("Updated existing restaurant with ID: {}", existingRestaurant.getId());
                            return existingRestaurant;
                        })
                        .orElseGet(() -> {
                            // Create new restaurant
                            Restaurant newRestaurant = createRestaurantFromRegistration(regResponse);
                            log.info("Created new restaurant from auth data");
                            return newRestaurant;
                        });

                restaurant = restaurantRepository.save(restaurant);
                return mapToResponse(restaurant);
            }
        } catch (Exception e) {
            log.error("Error during explicit sync from auth service", e);
            throw new RuntimeException("Error syncing with auth service: " + e.getMessage(), e);
        }

        throw new RestaurantNotFoundException(ownerId);
    }

    private void updateRestaurantFromAuth(Restaurant restaurant, RestaurantRegistrationResponse response) {
        restaurant.setName(response.getRestaurantName());
        restaurant.setAddress(response.getAddress());
        restaurant.setPhone(response.getPhone());
        restaurant.setEmail(response.getEmail());
        restaurant.setOwnerName(response.getOwnerName());
        restaurant.setNic(response.getNic());
        restaurant.setLatitude(response.getLatitude());
        restaurant.setLongitude(response.getLongitude());
        restaurant.setBankAccountOwner(response.getBankAccountOwner());
        restaurant.setBankName(response.getBankName());
        restaurant.setBranchName(response.getBranchName());
        restaurant.setAccountNumber(response.getAccountNumber());
        restaurant.setActive(response.isVerified());

        // Make sure admin_id is always set
        if (restaurant.getAdminId() == null) {
            restaurant.setAdminId(response.getEmail());
        }

        // Make sure password field is always set
        if (restaurant.getPassword() == null) {
            restaurant.setPassword("PLACEHOLDER_NOT_USED");
        }

        // Make sure username field is always set
        if (restaurant.getUsername() == null) {
            restaurant.setUsername(response.getEmail());
        }

        // Make sure owner_username field is set if it exists
        if (restaurant.getOwnerUsername() == null) {
            restaurant.setOwnerUsername(response.getEmail());
        }
    }
}