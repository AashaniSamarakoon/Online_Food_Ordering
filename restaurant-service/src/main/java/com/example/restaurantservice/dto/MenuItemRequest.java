package com.example.restaurantservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MenuItemRequest {
    @NotBlank(message = "Item name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "AVAILABLE|OUT_OF_STOCK|LIMITED_AVAILABILITY",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Status must be AVAILABLE, OUT_OF_STOCK or LIMITED_AVAILABILITY")
    private String status;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    private MultipartFile image;
}