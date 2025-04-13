package com.delivery.driverauthservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriverRegistrationDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String licenseNumber;
    private String vehicleType;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;

    // Additional vehicle information
    private String vehicleBrand;
    private String vehicleModel;
    private Integer vehicleYear;
    private String licensePlate;
    private String vehicleColor;
}