package com.delivery.driverservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long driverId; // Matching driverId from auth service

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @Column(nullable = false)
    private String vehicleType;

    private String vehicleBrand;

    private String vehicleModel;

    private Integer vehicleYear;

    private String licensePlate;

    private String vehicleColor;

    @Column(nullable = false)
    private String status; // AVAILABLE, BUSY, OFFLINE

    @Column(nullable = false)
    @Builder.Default
    private Double rating = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalTrips = 0;

    @Column(nullable = false)
    @Builder.Default
    private Double latitude = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double longitude = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    private LocalDateTime lastActiveAt;
}