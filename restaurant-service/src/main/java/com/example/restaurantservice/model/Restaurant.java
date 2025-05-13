package com.example.restaurantservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id")
    private String ownerId; // From JWT token (matches auth service user ID)

    @Column(name = "admin_id", nullable = false)
    private String adminId; // This should match the email or user_id from auth service

    // Add username field that's required by your database
    @Column(nullable = false)
    private String username; // Will be set to same as owner_id

    // Add owner_username field that might be required
    @Column(name = "owner_username")
    private String ownerUsername;

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String phone;

    @NotBlank @Email
    private String email;

    @NotBlank
    private String openingHours;

    @Column(nullable = false)
    private String password; // Required placeholder value

    @Column(nullable = false)
    private boolean isActive;

    // New fields based on RegisterRequest
    private String ownerName;

    private String nic;

    @Column(nullable = true)
    private Double latitude;

    @Column(nullable = true)
    private Double longitude;

    @Column(name = "bank_account_owner")
    private String bankAccountOwner;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "account_number")
    private String accountNumber;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menuItems;

    public void updateDetails(String name, String address, String phone,
                              String email, String openingHours) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.openingHours = openingHours;
    }

    // Extended update method to include all new fields
    public void updateFullDetails(String name, String address, String phone,
                                  String email, String openingHours,
                                  String ownerName, String nic,
                                  Double latitude, Double longitude,
                                  String bankAccountOwner, String bankName,
                                  String branchName, String accountNumber) {
        // Update basic details
        updateDetails(name, address, phone, email, openingHours);

        // Update additional fields
        this.ownerName = ownerName;
        this.nic = nic;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bankAccountOwner = bankAccountOwner;
        this.bankName = bankName;
        this.branchName = branchName;
        this.accountNumber = accountNumber;
    }
}