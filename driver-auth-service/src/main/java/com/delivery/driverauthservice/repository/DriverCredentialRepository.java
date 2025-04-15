package com.delivery.driverauthservice.repository;

import com.delivery.driverauthservice.model.DriverCredential;
import com.delivery.driverauthservice.model.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverCredentialRepository extends JpaRepository<DriverCredential, Long> {

    Optional<DriverCredential> findByUsername(String username);

    Optional<DriverCredential> findByPhoneNumber(String phoneNumber);

    // Add method to find by email
    Optional<DriverCredential> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    List<DriverCredential> findByRegistrationStatus(RegistrationStatus status);

    // Add method to check if email exists
    boolean existsByEmail(String email);

    Optional<DriverCredential> findByDriverId(Long tempDriverId);

    @Query("SELECT MAX(d.driverId) FROM DriverCredential d")
    Long findMaxDriverId();
}