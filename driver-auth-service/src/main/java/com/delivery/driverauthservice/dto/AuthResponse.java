package com.delivery.driverauthservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long driverId;
    private String username;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private Set<String> roles;
    private String tokenType;
    private boolean phoneVerified;
    private DriverDetailsDTO driverDetails;
    private VehicleDTO vehicleDetails;
    private List<DocumentDTO> documents;
}