package com.delivery.driverservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverDTO {
    private Long id;
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
    private String status;
    private Double rating;
    private Integer totalTrips;
    private Double latitude;
    private Double longitude;
    private Boolean isActive;
    private Boolean isVerified;
}