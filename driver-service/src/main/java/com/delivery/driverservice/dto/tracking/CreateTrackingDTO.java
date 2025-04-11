package com.delivery.driverservice.dto.tracking;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTrackingDTO {
    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Driver ID is required")
    private String driverId;
}