package com.delivery.driverauthservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "driver_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverCredential {

    @Id
    private Long driverId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    // Updated to include unique constraint and make it nullable=false
    @Column(nullable = false, unique = true)
    private String email;

    // Added first name field
    @Column(nullable = false)
    private String firstName;

    // Added last name field
    @Column(nullable = false)
    private String lastName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "driver_roles", joinColumns = @JoinColumn(name = "driver_id"))
    @Column(name = "role")
    private Set<String> roles;

    @Column(nullable = false)
    private boolean phoneVerified;

    @Column(nullable = false)
    private boolean accountLocked;

    private int failedLoginAttempts;

    private LocalDateTime lastLoginTime;

    private String deviceId;

    private String deviceToken;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status")
    private RegistrationStatus registrationStatus = RegistrationStatus.PENDING;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}