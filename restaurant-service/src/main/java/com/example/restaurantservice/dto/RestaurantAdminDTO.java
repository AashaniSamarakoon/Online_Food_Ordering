package com.example.restaurantservice.dto;

import lombok.Data;

@Data
public class RestaurantAdminDTO {
    private Long id;
    private String email;
    private String restaurantName;
    private String ownerName;
    private String nic;
    private String phone;
    private String address;
    private Double latitude;
    private Double longitude;
    private Boolean verified;
    private String bankAccountOwner;
    private String bankName;
    private String branchName;
    private String accountNumber;
    // Add any other fields you need
}