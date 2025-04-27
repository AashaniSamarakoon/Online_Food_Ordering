package com.example.restaurantservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RestaurantRequest {
    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$",
            message = "Phone number must be 10-15 digits and may start with +")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Opening hours are required")
    @Size(max = 100, message = "Opening hours cannot exceed 100 characters")
    private String openingHours;
}