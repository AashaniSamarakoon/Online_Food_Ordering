package com.delivery.driverauthservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendVerificationRequest {
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}