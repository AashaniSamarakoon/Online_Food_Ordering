package com.delivery.driverauthservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long driverId;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;

    // Authentication tokens
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;

    // Driver status
    private boolean phoneVerified;
    private String registrationStatus;
    private Set<String> roles;

    // Related data from auth service only
    private VehicleDTO vehicleDetails;
    private List<DocumentDTO> documents;

}