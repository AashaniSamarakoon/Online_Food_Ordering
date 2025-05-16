//package com.example.restaurantauth.controller;
//
//import com.example.restaurantauth.exception.AccountNotVerifiedException;
//import com.example.restaurantauth.exception.ResourceNotFoundException;
//import com.example.restaurantauth.model.RestaurantAdmin;
//import com.example.restaurantauth.service.RestaurantAdminService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/restaurant")
//@RequiredArgsConstructor
//public class RestaurantAdminController {
//    private final RestaurantAdminService adminService;
//
//    @GetMapping("/profile")
//    @PreAuthorize("hasRole('RESTAURANT_ADMIN') and #admin.isVerified()")
//    public ResponseEntity<Map<String, Object>> getProfile(
//            @AuthenticationPrincipal RestaurantAdmin admin) {
//
//        if (!admin.isVerified()) {
//            throw new AccountNotVerifiedException("Account not verified. Please contact support.");
//        }
//
//        return ResponseEntity.ok(buildRestaurantResponseMap(admin));
//    }
//
//    /**
//     * New endpoint to support service-to-service communication
//     * This will be called by the Restaurant Service via Feign client
//     */
//    @GetMapping("/by-owner/{ownerId}")
//    public ResponseEntity<Map<String, Object>> getRestaurantByOwner(@PathVariable String ownerId) {
//        RestaurantAdmin admin = adminService.findByEmail(ownerId)
//                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for owner: " + ownerId));
//
//        return ResponseEntity.ok(buildRestaurantResponseMap(admin));
//    }
//
//    /**
//     * Helper method to build a consistent response format
//     */
//    private Map<String, Object> buildRestaurantResponseMap(RestaurantAdmin admin) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("restaurantName", admin.getRestaurantName());
//        response.put("email", admin.getEmail());
//        response.put("ownerName", admin.getOwnerName());
//        response.put("phone", admin.getPhone());
//        response.put("address", admin.getAddress());
//        response.put("isVerified", admin.isVerified());
//        response.put("location", Map.of(
//                "lat", admin.getLatitude(),
//                "lng", admin.getLongitude()
//        ));
//        response.put("bankDetails", Map.of(
//                "accountOwner", admin.getBankAccountOwner(),
//                "bankName", admin.getBankName(),
//                "branchName", admin.getBranchName(),
//                "accountNumber", admin.getAccountNumber()
//        ));
//
//        return response;
//    }
//}


//package com.example.restaurantauth.controller;
//
//import com.example.restaurantauth.exception.ResourceNotFoundException;
//import com.example.restaurantauth.model.RestaurantAdmin;
//import com.example.restaurantauth.service.RestaurantAdminService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/restaurant")
//@RequiredArgsConstructor
//@Slf4j
//public class RestaurantAdminController {
//    private final RestaurantAdminService adminService;
//
//    @GetMapping("/profile")
//    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
//    public ResponseEntity<Map<String, Object>> getProfile(
//            @AuthenticationPrincipal RestaurantAdmin admin) {
//        // No need to check verification here as it's enforced during login
//        return ResponseEntity.ok(buildRestaurantResponseMap(admin));
//    }
//
//    @GetMapping("/by-owner/{ownerId}")
//    public ResponseEntity<Map<String, Object>> getRestaurantByOwner(@PathVariable String ownerId) {
//        RestaurantAdmin admin = adminService.findByEmail(ownerId)
//                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for owner: " + ownerId));
//
//        return ResponseEntity.ok(buildRestaurantResponseMap(admin));
//    }
//
//    @GetMapping("/admins/verified")
//    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
//    public ResponseEntity<List<Map<String, Object>>> getAllVerifiedRestaurants() {
//        return ResponseEntity.ok(
//                adminService.findAllVerifiedRestaurants().stream()
//                        .map(this::buildRestaurantResponseMap)
//                        .toList()
//        );
//    }
//
//
//
//    /**
//     * Helper method to build a consistent response format
//     */
//    private Map<String, Object> buildRestaurantResponseMap(RestaurantAdmin admin) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("restaurantName", admin.getRestaurantName());
//        response.put("email", admin.getEmail());
//        response.put("ownerName", admin.getOwnerName());
//        response.put("nic", admin.getNic());
//        response.put("phone", admin.getPhone());
//        response.put("address", admin.getAddress());
//        response.put("isVerified", admin.isVerified());
//
//        // Add location data if available
//        if (admin.getLatitude() != null && admin.getLongitude() != null) {
//            response.put("location", Map.of(
//                    "lat", admin.getLatitude(),
//                    "lng", admin.getLongitude()
//            ));
//        }
//
//        // Add bank details if available
//        if (admin.getBankAccountOwner() != null) {
//            response.put("bankAccountOwner", admin.getBankAccountOwner());
//            response.put("bankName", admin.getBankName());
//            response.put("branchName", admin.getBranchName());
//            response.put("accountNumber", admin.getAccountNumber());
//        }
//
//        return response;
//    }
//}




package com.example.restaurantauth.controller;

import com.example.restaurantauth.exception.ResourceNotFoundException;
import com.example.restaurantauth.model.RestaurantAdmin;
import com.example.restaurantauth.service.RestaurantAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
@Slf4j
public class RestaurantAdminController {
    private final RestaurantAdminService adminService;

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('RESTAURANT_ADMIN')")
    public ResponseEntity<Map<String, Object>> getProfile(
            @AuthenticationPrincipal RestaurantAdmin admin) {
        // No need to check verification here as it's enforced during login
        return ResponseEntity.ok(buildRestaurantResponseMap(admin));
    }

    @GetMapping("/by-owner/{ownerId}")
    public ResponseEntity<Map<String, Object>> getRestaurantByOwner(@PathVariable String ownerId) {
        RestaurantAdmin admin = adminService.findByEmail(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for owner: " + ownerId));

        return ResponseEntity.ok(buildRestaurantResponseMap(admin));
    }

    @GetMapping("/admins/verified")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllVerifiedRestaurantsForAdmin() {
        return ResponseEntity.ok(
                adminService.findAllVerifiedRestaurants().stream()
                        .map(this::buildRestaurantResponseMap)
                        .toList()
        );
    }

    /**
     * Public endpoint to get all verified restaurants
     * No authentication required
     */
    @GetMapping("/public/verified")
    public ResponseEntity<List<Map<String, Object>>> getAllVerifiedRestaurantsPublic() {
        log.info("Public request received for all verified restaurants");

        List<Map<String, Object>> verifiedRestaurants = adminService.findAllVerifiedRestaurants().stream()
                .map(this::buildPublicRestaurantResponseMap)
                .toList();

        log.info("Returning {} verified restaurants to public endpoint", verifiedRestaurants.size());
        return ResponseEntity.ok(verifiedRestaurants);
    }

    /**
     * Helper method to build a consistent response format for authenticated users
     */
    private Map<String, Object> buildRestaurantResponseMap(RestaurantAdmin admin) {
        Map<String, Object> response = new HashMap<>();
        response.put("restaurantName", admin.getRestaurantName());
        response.put("email", admin.getEmail());
        response.put("ownerName", admin.getOwnerName());
        response.put("nic", admin.getNic());
        response.put("phone", admin.getPhone());
        response.put("address", admin.getAddress());
        response.put("isVerified", admin.isVerified());

        // Add location data if available
        if (admin.getLatitude() != null && admin.getLongitude() != null) {
            response.put("location", Map.of(
                    "lat", admin.getLatitude(),
                    "lng", admin.getLongitude()
            ));
        }

        // Add bank details if available
        if (admin.getBankAccountOwner() != null) {
            response.put("bankAccountOwner", admin.getBankAccountOwner());
            response.put("bankName", admin.getBankName());
            response.put("branchName", admin.getBranchName());
            response.put("accountNumber", admin.getAccountNumber());
        }

        return response;
    }

    /**
     * Helper method to build a public response with limited data for non-authenticated users
     * Excludes sensitive information like NIC, bank details, etc.
     */
    private Map<String, Object> buildPublicRestaurantResponseMap(RestaurantAdmin admin) {
        Map<String, Object> response = new HashMap<>();
        response.put("restaurantName", admin.getRestaurantName());
        response.put("email", admin.getEmail());
        response.put("phone", admin.getPhone());
        response.put("address", admin.getAddress());

        // Add location data if available
        if (admin.getLatitude() != null && admin.getLongitude() != null) {
            response.put("location", Map.of(
                    "lat", admin.getLatitude(),
                    "lng", admin.getLongitude()
            ));
        }

//        // Include opening hours if available
//        if (admin.getOpeningHours() != null) {
//            response.put("openingHours", admin.getOpeningHours());
//        }

        return response;
    }
}