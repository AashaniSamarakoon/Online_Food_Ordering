package com.delivery.driverauthservice.service.impl;


import com.delivery.driverauthservice.client.DriverServiceClient;
import com.delivery.driverauthservice.dto.*;
import com.delivery.driverauthservice.exception.InvalidCredentialsException;
import com.delivery.driverauthservice.exception.ResourceAlreadyExistsException;
import com.delivery.driverauthservice.exception.TokenRefreshException;
import com.delivery.driverauthservice.exception.VerificationException;
import com.delivery.driverauthservice.model.DriverCredential;
import com.delivery.driverauthservice.model.RefreshToken;
import com.delivery.driverauthservice.model.Vehicle;
import com.delivery.driverauthservice.model.VerificationCode;
import com.delivery.driverauthservice.repository.DriverCredentialRepository;
import com.delivery.driverauthservice.repository.RefreshTokenRepository;
import com.delivery.driverauthservice.repository.VehicleRepository;
import com.delivery.driverauthservice.repository.VerificationCodeRepository;
import com.delivery.driverauthservice.security.CustomUserDetails;
import com.delivery.driverauthservice.security.JwtTokenProvider;
import com.delivery.driverauthservice.security.TokenBlacklistService;
import com.delivery.driverauthservice.service.AuthService;
import com.delivery.driverauthservice.service.SmsService;
import com.delivery.driverauthservice.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final DriverCredentialRepository driverCredentialRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final SmsService smsService;
    private final DriverServiceClient driverServiceClient;
    private final VehicleService vehicleService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        // Authenticate with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Check if phone is verified
        if (!userDetails.isPhoneVerified()) {
            throw new VerificationException("Phone number not verified. Please verify your phone number.");
        }

        // Get driver from database
        DriverCredential driver = driverCredentialRepository.findById(userDetails.getDriverId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        // Update device information if provided
        if (loginRequest.getDeviceId() != null) {
            driver.setDeviceId(loginRequest.getDeviceId());
        }

        if (loginRequest.getDeviceToken() != null) {
            driver.setDeviceToken(loginRequest.getDeviceToken());
        }

        driver.setLastLoginTime(LocalDateTime.now());
        driver.setFailedLoginAttempts(0); // Reset failed attempts on successful login
        driverCredentialRepository.save(driver);

        // Generate tokens
        String accessToken = tokenProvider.generateToken(driver);
        RefreshToken refreshToken = createRefreshToken(driver.getDriverId(), loginRequest.getDeviceId());

        // Get driver details from driver service
        DriverDetailsDTO driverDetails = driverServiceClient.getDriverDetails(driver.getDriverId());

        // Update driver status to ONLINE
        driverServiceClient.updateDriverStatus(driver.getDriverId(), "ONLINE");

        // Get vehicle details
        VehicleDTO vehicleDetails = vehicleService.getVehicleByDriverId(driver.getDriverId());

        // Create and return the authentication response
        return AuthResponse.builder()
                .driverId(driver.getDriverId())
                .username(driver.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(tokenProvider.getTokenExpirationInSeconds())
                .roles(driver.getRoles())
                .tokenType("Bearer")
                .phoneVerified(driver.isPhoneVerified())
                .driverDetails(driverDetails)
                .vehicleDetails(vehicleDetails)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegistrationRequest registrationRequest) {
        // Check if username already exists
        if (driverCredentialRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        // Check if phone number already exists
        if (driverCredentialRepository.existsByPhoneNumber(registrationRequest.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Phone number already registered");
        }

        // First register driver in driver service
        DriverRegistrationDTO driverRegistrationDTO = DriverRegistrationDTO.builder()
                .name(registrationRequest.getFullName())
                .licenseNumber(registrationRequest.getLicenseNumber())
                .vehicleType(registrationRequest.getVehicleType())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .latitude(0.0)  // Default values until driver provides location
                .longitude(0.0)
                .vehicleBrand(registrationRequest.getVehicleBrand())
                .vehicleModel(registrationRequest.getVehicleModel())
                .vehicleYear(registrationRequest.getVehicleYear())
                .licensePlate(registrationRequest.getLicensePlate())
                .vehicleColor(registrationRequest.getVehicleColor())
                .build();

        DriverDetailsDTO driverDetails = driverServiceClient.registerDriver(driverRegistrationDTO);

        // Create driver credentials
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_DRIVER");

        DriverCredential driverCredential = DriverCredential.builder()
                .driverId(driverDetails.getId())
                .username(registrationRequest.getUsername())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .email(registrationRequest.getEmail())
                .roles(roles)
                .phoneVerified(false)  // Requires verification
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();

        driverCredentialRepository.save(driverCredential);

        // Register vehicle information
        Vehicle vehicle = Vehicle.builder()
                .driver(driverCredential)
                .brand(registrationRequest.getVehicleBrand())
                .model(registrationRequest.getVehicleModel())
                .year(registrationRequest.getVehicleYear())
                .licensePlate(registrationRequest.getLicensePlate())
                .color(registrationRequest.getVehicleColor())
                .vehicleType(registrationRequest.getVehicleType())
                .verified(false)
                .build();

        vehicleRepository.save(vehicle);

        // Send verification code
        sendVerificationCode(new SendVerificationRequest(registrationRequest.getPhoneNumber()));

        // Return response without tokens (user needs to verify phone first)
        VehicleDTO vehicleDTO = VehicleDTO.builder()
                .id(vehicle.getId())
                .driverId(driverCredential.getDriverId())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .licensePlate(vehicle.getLicensePlate())
                .color(vehicle.getColor())
                .vehicleType(vehicle.getVehicleType())
                .verified(vehicle.getVerified())
                .build();

        return AuthResponse.builder()
                .driverId(driverCredential.getDriverId())
                .username(driverCredential.getUsername())
                .phoneVerified(false)
                .roles(roles)
                .driverDetails(driverDetails)
                .vehicleDetails(vehicleDTO)
                .build();
    }

    @Override
    @Transactional
    public boolean sendVerificationCode(SendVerificationRequest request) {
        // Generate a 6-digit code
        String code = RandomStringUtils.randomNumeric(6);

        // Create verification code record
        VerificationCode verificationCode = VerificationCode.builder()
                .phoneNumber(request.getPhoneNumber())
                .code(code)
                .expiryTime(LocalDateTime.now().plusMinutes(15))  // Valid for 15 minutes
                .used(false)
                .attempts(0)
                .build();

        verificationCodeRepository.save(verificationCode);

        // Send SMS with verification code
        String message = "Your verification code is: " + code + ". It will expire in 15 minutes.";
        return smsService.sendSms(request.getPhoneNumber(), message);
    }

    @Override
    @Transactional
    public boolean verifyPhoneNumber(PhoneVerificationRequest request) {
        // Find active verification code
        Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository.findActiveCodeByPhoneNumber(
                request.getPhoneNumber(), LocalDateTime.now());

        if (verificationCodeOpt.isEmpty()) {
            throw new VerificationException("No active verification code found. Please request a new code.");
        }

        VerificationCode verificationCode = verificationCodeOpt.get();

        // Check if code is expired
        if (verificationCode.isExpired()) {
            throw new VerificationException("Verification code expired. Please request a new code.");
        }

        // Check if max attempts reached
        if (verificationCode.getAttempts() >= 3) {
            throw new VerificationException("Too many incorrect attempts. Please request a new code.");
        }

        // Validate code
        if (!verificationCode.getCode().equals(request.getCode())) {
            verificationCode.setAttempts(verificationCode.getAttempts() + 1);
            verificationCodeRepository.save(verificationCode);
            throw new VerificationException("Invalid verification code. " +
                    (3 - verificationCode.getAttempts()) + " attempts remaining.");
        }

        // Mark code as used
        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);

        // Update driver phone verification status
        Optional<DriverCredential> driverOpt = driverCredentialRepository.findByPhoneNumber(request.getPhoneNumber());
        if (driverOpt.isPresent()) {
            DriverCredential driver = driverOpt.get();
            driver.setPhoneVerified(true);
            driverCredentialRepository.save(driver);
        }

        return true;
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String requestRefreshToken = refreshTokenRequest.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));

        // Check if token is expired
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token expired. Please login again.");
        }

        // Check device ID if provided
        if (refreshTokenRequest.getDeviceId() != null &&
                !refreshTokenRequest.getDeviceId().equals(refreshToken.getDeviceId())) {
            throw new TokenRefreshException("Invalid device. Please login again.");
        }

        // Get driver
        DriverCredential driver = driverCredentialRepository.findById(refreshToken.getDriverId())
                .orElseThrow(() -> new TokenRefreshException("User not found"));

        // Generate new tokens
        String accessToken = tokenProvider.generateToken(driver);
        RefreshToken newRefreshToken = createRefreshToken(driver.getDriverId(), refreshToken.getDeviceId());

        // Delete old refresh token
        refreshTokenRepository.delete(refreshToken);

        // Get driver details
        DriverDetailsDTO driverDetails = driverServiceClient.getDriverDetails(driver.getDriverId());

        // Create and return the authentication response
        return AuthResponse.builder()
                .driverId(driver.getDriverId())
                .username(driver.getUsername())
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .expiresIn(tokenProvider.getTokenExpirationInSeconds())
                .roles(driver.getRoles())
                .tokenType("Bearer")
                .phoneVerified(driver.isPhoneVerified())
                .driverDetails(driverDetails)
                .build();
    }

    @Override
    @Transactional
    public boolean resetPassword(PasswordResetRequest resetRequest) {
        // Verify code first
        PhoneVerificationRequest verificationRequest = new PhoneVerificationRequest();
        verificationRequest.setPhoneNumber(resetRequest.getPhoneNumber());
        verificationRequest.setCode(resetRequest.getVerificationCode());

        boolean verified = verifyPhoneNumber(verificationRequest);

        if (!verified) {
            return false;
        }

        // Find driver by phone number
        DriverCredential driver = driverCredentialRepository.findByPhoneNumber(resetRequest.getPhoneNumber())
                .orElseThrow(() -> new VerificationException("No account found with this phone number"));

        // Update password
        driver.setPassword(passwordEncoder.encode(resetRequest.getNewPassword()));
        driverCredentialRepository.save(driver);

        return true;
    }

    @Override
    @Transactional
    public boolean logout(Authentication authentication, String token) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long driverId = userDetails.getDriverId();

            // Update driver status
            try {
                driverServiceClient.updateDriverStatus(driverId, "OFFLINE");
            } catch (Exception e) {
                log.error("Error updating driver status to OFFLINE: {}", e.getMessage());
            }
        }

        // Blacklist current access token
        if (token != null && !token.isEmpty()) {
            tokenBlacklistService.blacklistToken(token, tokenProvider.getTokenExpirationInSeconds());
            return true;
        }

        return false;
    }

    private RefreshToken createRefreshToken(Long driverId, String deviceId) {
        // Delete existing refresh tokens for this device
        if (deviceId != null) {
            refreshTokenRepository.deleteByDriverIdAndDeviceId(driverId, deviceId);
        }

        // Create new refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .driverId(driverId)
                .token(UUID.randomUUID().toString())
                .deviceId(deviceId != null ? deviceId : "unknown")
                .expiryDate(LocalDateTime.now().plusDays(30))  // 30 days validity
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}