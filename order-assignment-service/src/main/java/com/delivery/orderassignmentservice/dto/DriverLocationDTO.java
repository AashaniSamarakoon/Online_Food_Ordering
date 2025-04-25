package com.delivery.orderassignmentservice.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class DriverLocationDTO {
    private String driverId;
    private double latitude;
    private double longitude;
    private double distance;  // distance in meters from requested coordinates
    private double heading;   // direction in degrees (0-359)
    private double speed;     // speed in m/s
    private Instant timestamp;
}