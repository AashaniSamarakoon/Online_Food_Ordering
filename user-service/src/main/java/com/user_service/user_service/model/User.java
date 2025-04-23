package com.user_service.user_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users") // still avoiding 'user' keyword
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;  // can also be email or phone, if unique

    @Column(nullable = false)
    private String password;

    private String firstName;

    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    private String profileImageUrl;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    // Coordinates for delivery (optional, if preselected by user)
    private Double latitude;
    private Double longitude;

    private boolean isVerified = false;
    private boolean isActive = true;
}
