package com.delivery.driverservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDTO {
    private Long id;
    private String name;
    private String licenseNumber;
    private String vehicleType;
    private String status;
    private Double rating;
    private Double latitude;
    private Double longitude;
    private Boolean isActive;
}