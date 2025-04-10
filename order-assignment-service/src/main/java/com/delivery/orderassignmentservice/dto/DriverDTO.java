package com.delivery.orderassignmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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