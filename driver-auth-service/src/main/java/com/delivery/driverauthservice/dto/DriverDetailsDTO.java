package com.delivery.driverauthservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDetailsDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String licenseNumber;
    private String vehicleType;
    private String status;
    private Double rating;
    private Boolean isActive;
}