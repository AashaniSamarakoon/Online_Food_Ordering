package com.delivery.driverauthservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    // Changed from 'username' to more generic 'loginIdentifier'
    // This can be username, email, or phone number
    @NotBlank(message = "Login identifier is required")
    private String loginIdentifier;

    @NotBlank(message = "Password is required")
    private String password;

    private String deviceId;
    private String deviceToken;
}