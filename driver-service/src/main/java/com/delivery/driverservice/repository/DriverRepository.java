package com.delivery.driverservice.repository;

import com.delivery.driverservice.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByStatus(String status);

    List<Driver> findByIsActive(Boolean isActive);

    @Query("SELECT d FROM Driver d WHERE d.status = 'AVAILABLE' AND d.isActive = true")
    List<Driver> findAvailableDrivers();

    @Query(value = "SELECT * FROM drivers d WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(d.latitude)) * " +
            "cos(radians(d.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(d.latitude)))) < :radius " +
            "AND d.status = 'AVAILABLE' AND d.is_active = true",
            nativeQuery = true)
    List<Driver> findNearbyAvailableDrivers(Double lat, Double lng, Double radius);
}