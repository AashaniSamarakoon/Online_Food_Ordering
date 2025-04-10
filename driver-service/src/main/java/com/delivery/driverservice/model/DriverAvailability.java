package com.delivery.driverservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DriverAvailability {
    private Long driverId;
    private Boolean isAvailable;
    private String status;
}