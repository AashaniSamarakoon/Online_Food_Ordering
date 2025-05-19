package com.order_service.order_service.dto;

import lombok.Data;

@Data
public class RestaurantDetails {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String openingHours;
    private String ownerUsername;
    private String adminId;
    private String username;
    private String ownerName;
    private String nic;
    private Double latitude;
    private Double longitude;
    private String bankAccountOwner;
    private String bankName;
    private String branchName;
    private String accountNumber;
    private boolean active;
}
