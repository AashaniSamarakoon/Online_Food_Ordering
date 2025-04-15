package com.delivery.driverservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "driver_id", unique = true)
    private Long driverId;

    @Column(name = "username")
    private String username;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "vehicle_brand")
    private String vehicleBrand;

    @Column(name = "vehicle_model")
    private String vehicleModel;

    @Column(name = "vehicle_year")
    private Integer vehicleYear;

    @Column(name = "license_plate")
    private String licensePlate;

    @Column(name = "vehicle_color")
    private String vehicleColor;

    @Column(name = "status")
    private String status;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "total_trips")
    private Integer totalTrips;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;
}