package com.delivery.driverservice.dto.tracking;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrackingDTO {
    private String id;
    private String orderId;
    private String driverId;
    private String status; // PENDING, IN_PROGRESS, COMPLETED
    private LocationDTO currentLocation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class LocationDTO {
        private Double lat;
        private Double lng;
    }
}