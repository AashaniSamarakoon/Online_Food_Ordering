package com.delivery.driverauthservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "driver_id", unique = true, nullable = false)
    private DriverCredential driver;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    private String color;

    @Column(nullable = false)
    private String vehicleType; // CAR, MOTORCYCLE, SCOOTER, etc.

    // Make sure the verified field is properly defined and has correct getters/setters
    @Column(nullable = false)
    private Boolean verified = false;

    // If needed, you can add these explicit methods to ensure proper behavior
    public boolean isVerified() {
        return verified != null && verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (verified == null) {
            verified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}