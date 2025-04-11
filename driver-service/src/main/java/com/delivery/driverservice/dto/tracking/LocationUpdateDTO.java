package com.delivery.driverservice.dto.tracking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationUpdateDTO {
    @NotNull(message = "Latitude is required")
    private Double lat;

    @NotNull(message = "Longitude is required")
    private Double lng;
}