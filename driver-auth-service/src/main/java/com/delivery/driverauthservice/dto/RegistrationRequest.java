package com.delivery.driverauthservice.dto;

import com.delivery.driverauthservice.model.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    // Vehicle details
    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotBlank(message = "Vehicle brand is required")
    private String vehicleBrand;

    @NotBlank(message = "Vehicle model is required")
    private String vehicleModel;

    private Integer vehicleYear;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    private String vehicleColor;

    // Documents as a map where key is DocumentType and value is base64 encoded image
    private Map<DocumentType, DocumentUploadMetadata> documents;
}