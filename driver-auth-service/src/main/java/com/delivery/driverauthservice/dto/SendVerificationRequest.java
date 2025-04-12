package com.delivery.driverauthservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendVerificationRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    // Default constructor (already exists)
    public SendVerificationRequest() {
    }

    // Add this new constructor
    public SendVerificationRequest(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}