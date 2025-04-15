package com.delivery.driverauthservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverVehicleVerificationRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    // Using JsonProperty to ensure proper mapping from JSON
    @NotNull(message = "Validation status is required")
    @JsonProperty("isValid")
    private Boolean valid; // Renamed to avoid the "is" prefix causing getter/setter issues

    // Add a convenience method to get the value safely
    public boolean isValid() {
        return valid != null && valid;
    }
}