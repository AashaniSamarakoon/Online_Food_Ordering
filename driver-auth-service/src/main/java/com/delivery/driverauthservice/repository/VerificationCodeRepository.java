package com.delivery.driverauthservice.repository;

import com.delivery.driverauthservice.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    @Query("SELECT v FROM VerificationCode v WHERE v.phoneNumber = ?1 AND v.used = false AND v.expiryTime > ?2 ORDER BY v.createdAt DESC")
    Optional<VerificationCode> findActiveCodeByPhoneNumber(String phoneNumber, LocalDateTime now);
}