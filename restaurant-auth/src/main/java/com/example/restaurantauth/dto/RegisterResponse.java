package com.example.restaurantauth.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private String message;
    private String email;
    private String restaurantName;
    private boolean isVerified;
}