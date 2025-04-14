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
public class DriverDocumentVerificationRequest {

    @NotNull(message = "Document ID is required")
    private Long documentId;

    // The problem is here - the field name in the JSON is probably "isValid" (with lowercase 'i')
    // but the getter method generated is getIsValid() (with uppercase 'I')
    @NotNull(message = "Validation status is required")
    @JsonProperty("isValid") // This annotation ensures proper JSON mapping
    private Boolean valid; // Renamed to avoid the "is" prefix causing getter/setter issues

    private String remarks;

    // Add a convenience method to get the value safely
    public boolean isValid() {
        return valid != null && valid;
    }
}