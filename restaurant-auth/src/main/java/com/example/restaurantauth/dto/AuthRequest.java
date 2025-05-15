package com.example.restaurantauth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;
}