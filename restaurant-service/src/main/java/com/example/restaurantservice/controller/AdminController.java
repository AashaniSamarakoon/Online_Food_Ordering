package com.example.restaurantservice.controller;

import com.example.restaurantservice.model.Restaurant;
import com.example.restaurantservice.service.RestaurantSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/super/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final RestaurantSyncService restaurantSyncService;

    @PostMapping("/sync/restaurants")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> triggerRestaurantSync(
            @RequestHeader("Authorization") String bearerToken) {

        log.info("Manual sync of restaurants triggered by admin");
        List<Restaurant> syncedRestaurants = restaurantSyncService.syncAllVerifiedRestaurants(bearerToken);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Restaurant sync completed",
                "syncedCount", syncedRestaurants.size()
        ));
    }
}