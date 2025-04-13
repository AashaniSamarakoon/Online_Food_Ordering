package com.delivery.driverservice.repository;

import com.delivery.driverservice.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    // Find by driverId (matches the ID from auth service)
    Optional<Driver> findByDriverId(Long driverId);

    // Find drivers by status
    List<Driver> findByStatus(String status);

    // Find active/inactive drivers
    List<Driver> findByIsActive(Boolean isActive);

    // Find by status and active flag
    List<Driver> findByStatusAndIsActiveTrue(String status);

    // Find by verification status
    List<Driver> findByIsVerified(Boolean isVerified);

    // Find available drivers
    @Query("SELECT d FROM Driver d WHERE d.status = 'AVAILABLE' AND d.isActive = true")
    List<Driver> findAvailableDrivers();

    // Find verified and available drivers
    @Query("SELECT d FROM Driver d WHERE d.status = 'AVAILABLE' AND d.isActive = true AND d.isVerified = true")
    List<Driver> findVerifiedAvailableDrivers();

    // Find nearby available drivers
    @Query(value = "SELECT * FROM drivers d WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(d.latitude)) * " +
            "cos(radians(d.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(d.latitude)))) < :radius " +
            "AND d.status = 'AVAILABLE' AND d.is_active = true",
            nativeQuery = true)
    List<Driver> findNearbyAvailableDrivers(Double lat, Double lng, Double radius);

    // Find nearby available drivers of specific vehicle type
    @Query(value = "SELECT * FROM drivers d WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(d.latitude)) * " +
            "cos(radians(d.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(d.latitude)))) < :radius " +
            "AND d.status = 'AVAILABLE' AND d.is_active = true " +
            "AND d.vehicle_type = :vehicleType",
            nativeQuery = true)
    List<Driver> findNearbyAvailableDriversByVehicleType(Double lat, Double lng, Double radius, String vehicleType);

    // Find by phone number
    Optional<Driver> findByPhoneNumber(String phoneNumber);

    // Find by license number
    Optional<Driver> findByLicenseNumber(String licenseNumber);
}