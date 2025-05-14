package com.delivery.driverservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DriverStatusUpdate {
    private Long driverId;
    private String status;
    private LocalDateTime lastActiveAt;
}