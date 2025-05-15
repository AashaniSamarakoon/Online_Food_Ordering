package com.delivery.orderassignmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DriverWithDetails {
    private DriverLocationDTO location;
    private DriverDTO driverDetails;
}