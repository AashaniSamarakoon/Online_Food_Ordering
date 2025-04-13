package com.delivery.driverservice.dto;

import lombok.Data;

@Data
public class DriverRequest {
    private Long driverId;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String licenseNumber;
    private String vehicleType;
    private String vehicleBrand;
    private String vehicleModel;
    private Integer vehicleYear;
    private String licensePlate;
    private String vehicleColor;
    private Double latitude;
    private Double longitude;
}