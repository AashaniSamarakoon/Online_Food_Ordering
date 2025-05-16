package com.example.restaurantservice.config;

import com.example.restaurantservice.service.RestaurantSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    private final RestaurantSyncService restaurantSyncService;
    private final ServiceAuthConfig serviceAuthConfig;

    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledRestaurantSync() {
        try {
            // Get a fresh service token via method in ServiceAuthConfig
            String serviceToken = serviceAuthConfig.getServiceToken();
            log.info("Starting scheduled restaurant sync");
            restaurantSyncService.syncAllVerifiedRestaurants(serviceToken);
            log.info("Completed scheduled restaurant sync");
        } catch (Exception e) {
            log.error("Scheduled restaurant sync failed: {}", e.getMessage());
        }
    }
}