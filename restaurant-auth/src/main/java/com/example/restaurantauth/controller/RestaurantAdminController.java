package com.example.restaurantauth.controller;

import com.example.restaurantauth.exception.AccountNotVerifiedException;
import com.example.restaurantauth.exception.ResourceNotFoundException;
import com.example.restaurantauth.model.RestaurantAdmin;
import com.example.restaurantauth.service.RestaurantAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class RestaurantAdminController {
    private final RestaurantAdminService adminService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') and #admin.isVerified()")
    public ResponseEntity<Map<String, Object>> getProfile(
            @AuthenticationPrincipal RestaurantAdmin admin) {

        if (!admin.isVerified()) {
            throw new AccountNotVerifiedException("Account not verified. Please contact support.");
        }

        return ResponseEntity.ok(buildRestaurantResponseMap(admin));
    }

    /**
     * New endpoint to support service-to-service communication
     * This will be called by the Restaurant Service via Feign client
     */
    @GetMapping("/by-owner/{ownerId}")
    public ResponseEntity<Map<String, Object>> getRestaurantByOwner(@PathVariable String ownerId) {
        RestaurantAdmin admin = adminService.findByEmail(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for owner: " + ownerId));

        return ResponseEntity.ok(buildRestaurantResponseMap(admin));
    }

    /**
     * Helper method to build a consistent response format
     */
    private Map<String, Object> buildRestaurantResponseMap(RestaurantAdmin admin) {
        Map<String, Object> response = new HashMap<>();
        response.put("restaurantName", admin.getRestaurantName());
        response.put("email", admin.getEmail());
        response.put("ownerName", admin.getOwnerName());
        response.put("phone", admin.getPhone());
        response.put("address", admin.getAddress());
        response.put("isVerified", admin.isVerified());
        response.put("location", Map.of(
                "lat", admin.getLatitude(),
                "lng", admin.getLongitude()
        ));
        response.put("bankDetails", Map.of(
                "accountOwner", admin.getBankAccountOwner(),
                "bankName", admin.getBankName(),
                "branchName", admin.getBranchName(),
                "accountNumber", admin.getAccountNumber()
        ));

        return response;
    }
}