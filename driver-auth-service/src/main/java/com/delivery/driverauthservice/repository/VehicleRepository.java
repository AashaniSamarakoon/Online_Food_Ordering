package com.delivery.driverauthservice.repository;

import com.delivery.driverauthservice.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByDriverDriverId(Long driverId);
    boolean existsByLicensePlate(String licensePlate);

    @Query("SELECT v FROM Vehicle v WHERE v.driver.driverId = :driverId")
    Optional<Vehicle> findByDriverId(Long driverId);}