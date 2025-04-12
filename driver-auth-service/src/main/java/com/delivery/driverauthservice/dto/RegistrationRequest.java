package com.delivery.driverauthservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegistrationRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Password must be at least 8 characters and include a number, uppercase, lowercase, and special character")
    private String password;

    private String email;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotBlank(message = "Full name is required")
    private String fullName;

    // Vehicle details
    @NotBlank(message = "Vehicle brand is required")
    private String vehicleBrand;

    @NotBlank(message = "Vehicle model is required")
    private String vehicleModel;

    @NotNull(message = "Vehicle year is required")
    private Integer vehicleYear;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    private String vehicleColor;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;
}