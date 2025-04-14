package com.delivery.driverservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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