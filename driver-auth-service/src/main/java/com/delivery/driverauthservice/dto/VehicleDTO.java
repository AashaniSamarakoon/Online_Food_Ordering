package com.delivery.driverauthservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDTO {
    private Long id;
    private Long driverId;
    private String brand;
    private String model;
    private Integer year;
    private String licensePlate;
    private String color;
    private String vehicleType;
    private Boolean verified;
}