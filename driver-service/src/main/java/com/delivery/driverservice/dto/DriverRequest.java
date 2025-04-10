package com.delivery.driverservice.dto;

import lombok.Data;

@Data
public class DriverRequest {
    private String name;
    private String licenseNumber;
    private String vehicleType;
    private Double latitude;
    private Double longitude;
}