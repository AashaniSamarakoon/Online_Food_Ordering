package com.delivery.driverauthservice.repository;

import com.delivery.driverauthservice.model.DriverCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverCredentialRepository extends JpaRepository<DriverCredential, Long> {

    Optional<DriverCredential> findByUsername(String username);

    Optional<DriverCredential> findByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);
}