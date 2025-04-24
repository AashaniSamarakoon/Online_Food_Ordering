package com.example.restaurantservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MenuItemRequest {
    @NotBlank
    private String name;

    private String description;

    @Positive
    private Double price;

    @NotBlank
    private String category;

    private Boolean available = true;
}