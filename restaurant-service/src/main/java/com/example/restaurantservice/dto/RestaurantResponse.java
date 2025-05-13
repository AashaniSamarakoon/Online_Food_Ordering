package com.example.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private boolean isActive;
    private String openingHours;
    private String ownerUsername;
    private String adminId;
    private String username;  // Add this field

    // New fields
    private String ownerName;
    private String nic;
    private Double latitude;
    private Double longitude;
    private String bankAccountOwner;
    private String bankName;
    private String branchName;
    private String accountNumber;
}