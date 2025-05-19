package com.delivery.orderassignmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverAssignmentDTO {
    private String driverId;
    private String name;
    private String licenseNumber;
    private String vehicleType;
    private Double rating;
    private String phone;
}