package com.example.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String username;
    private String name;
    private String address;
    private String phone;
    private String email;
    private Boolean isActive;
    private String openingHours;
    private String ownerUsername;  // Changed from adminId to ownerUsername
    private List<MenuItemResponse> menuItems;
}