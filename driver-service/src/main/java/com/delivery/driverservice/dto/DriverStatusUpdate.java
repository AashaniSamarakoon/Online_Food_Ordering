package com.delivery.driverservice.dto;

import lombok.Data;

@Data
public class DriverStatusUpdate {
    private Long driverId;
    private String status;
    private Double latitude;
    private Double longitude;
}