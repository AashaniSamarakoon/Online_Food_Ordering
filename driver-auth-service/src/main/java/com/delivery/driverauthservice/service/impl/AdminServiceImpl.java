//package com.delivery.driverauthservice.service.impl;
//
//import com.delivery.driverauthservice.dto.*;
//import com.delivery.driverauthservice.exception.ResourceAlreadyExistsException;
//import com.delivery.driverauthservice.messaging.DriverEventPublisher;
//import com.delivery.driverauthservice.model.*;
//import com.delivery.driverauthservice.repository.DriverCredentialRepository;
//import com.delivery.driverauthservice.repository.RefreshTokenRepository;
//import com.delivery.driverauthservice.security.JwtTokenProvider;
//import com.delivery.driverauthservice.service.AdminService;
//import com.delivery.driverauthservice.service.SequenceGenerator;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class AdminServiceImpl implements AdminService {
//
//    private final DriverCredentialRepository driverCredentialRepository;
//    private final RefreshTokenRepository refreshTokenRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtTokenProvider tokenProvider;
//    private final SequenceGenerator sequenceGenerator;
//    private final DriverEventPublisher driverEventPublisher;
//
//    @Override
//    public AuthResponse login(LoginRequest loginRequest) {
//        return null;
//    }
//
//    @Override
//    @Transactional
//    public AuthResponse registerAdmin(AdminRegistrationRequest registrationRequest) {
//        // Check if username already exists
//        if (driverCredentialRepository.existsByUsername(registrationRequest.getUsername())) {
//            throw new ResourceAlreadyExistsException("Username already exists");
//        }
//
//        // Check if email already exists (if provided)
//        if (registrationRequest.getEmail() != null && !registrationRequest.getEmail().isEmpty() &&
//                driverCredentialRepository.existsByEmail(registrationRequest.getEmail())) {
//            throw new ResourceAlreadyExistsException("Email already registered");
//        }
//
//        // Generate an ID for the admin
//        Long adminId = sequenceGenerator.nextId();
//
//        // Create admin credentials
//        Set<String> roles = new HashSet<>();
//        roles.add("ROLE_ADMIN");
//
//        DriverCredential adminCredential = DriverCredential.builder()
//                .driverId(adminId)
//                .username(registrationRequest.getUsername())
//                .password(passwordEncoder.encode(registrationRequest.getPassword()))
//                .phoneNumber(registrationRequest.getPhoneNumber())
//                .email(registrationRequest.getEmail())
//                .firstName(registrationRequest.getFirstName())
//                .lastName(registrationRequest.getLastName())
//                .roles(roles)
//                .phoneVerified(true)  // Admins don't need phone verification
//                .accountLocked(false)
//                .registrationStatus(RegistrationStatus.COMPLETED)  // Admin accounts are immediately active
//                .failedLoginAttempts(0)
//                .build();
//
//        driverCredentialRepository.save(adminCredential);
//
//        // Generate tokens
//        String accessToken = tokenProvider.generateToken(adminCredential);
//        RefreshToken refreshToken = createRefreshToken(adminCredential.getDriverId(), null);
//
//        // Return response with tokens
//        return AuthResponse.builder()
//                .driverId(adminCredential.getDriverId())
//                .username(adminCredential.getUsername())
//                .firstName(adminCredential.getFirstName())
//                .lastName(adminCredential.getLastName())
//                .phoneNumber(adminCredential.getPhoneNumber())
//                .email(adminCredential.getEmail())
//                .accessToken(accessToken)
//                .refreshToken(refreshToken.getToken())
//                .expiresIn(tokenProvider.getTokenExpirationInSeconds())
//                .tokenType("Bearer")
//                .phoneVerified(true)
//                .registrationStatus(RegistrationStatus.COMPLETED.name())
//                .roles(roles)
//                .build();
//    }
//
//
//    private RefreshToken createRefreshToken(Long driverId, String deviceId) {
//        // Delete existing refresh tokens for this device
//        if (deviceId != null) {
//            refreshTokenRepository.deleteByDriverIdAndDeviceId(driverId, deviceId);
//        }
//
//        // Create new refresh token
//        RefreshToken refreshToken = RefreshToken.builder()
//                .driverId(driverId)
//                .token(UUID.randomUUID().toString())
//                .deviceId(deviceId != null ? deviceId : "unknown")
//                .expiryDate(LocalDateTime.now().plusDays(30))  // 30 days validity
//                .build();
//
//        return refreshTokenRepository.save(refreshToken);
//    }
//}