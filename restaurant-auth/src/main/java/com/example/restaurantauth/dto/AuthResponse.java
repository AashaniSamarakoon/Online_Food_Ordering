package com.example.restaurantauth.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private String email;
    private String restaurantName;
    private String role;
    private boolean isVerified;
    private long expiresIn; // in seconds

    // Static factory method for success response
    public static AuthResponse success(String token, String email, String restaurantName,
                                       String role, boolean isVerified, long expiresIn) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(email)
                .restaurantName(restaurantName)
                .role(role)
                .isVerified(isVerified)
                .expiresIn(expiresIn)
                .build();
    }

    // Static factory method for failure response
    public static AuthResponse failure(String message) {
        return AuthResponse.builder()
                .token(null)
                .tokenType(null)
                .email(null)
                .restaurantName(null)
                .role(null)
                .isVerified(false)
                .expiresIn(0)
                .build();
    }
}