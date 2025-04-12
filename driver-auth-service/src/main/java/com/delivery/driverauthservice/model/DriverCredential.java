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

    private String email;

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