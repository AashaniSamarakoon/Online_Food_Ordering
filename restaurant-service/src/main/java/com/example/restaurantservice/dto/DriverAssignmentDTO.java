package com.example.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignmentDTO {
    private Long driverId;
    private Long orderId;
    private String driverName;
    private String driverPhone;
    private String vehicleNumber;
    private String status;
    private Double latitude;
    private Double longitude;
    private String message;
}
