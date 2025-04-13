package com.delivery.driverauthservice.service.impl;

import com.delivery.driverauthservice.dto.*;
import com.delivery.driverauthservice.exception.InvalidCredentialsException;
import com.delivery.driverauthservice.exception.ResourceAlreadyExistsException;
import com.delivery.driverauthservice.exception.TokenRefreshException;
import com.delivery.driverauthservice.exception.VerificationException;
import com.delivery.driverauthservice.messaging.DriverEventPublisher;
import com.delivery.driverauthservice.messaging.DriverRegistrationEvent;
import com.delivery.driverauthservice.model.*;
import com.delivery.driverauthservice.repository.DriverCredentialRepository;
import com.delivery.driverauthservice.repository.RefreshTokenRepository;
import com.delivery.driverauthservice.repository.VehicleRepository;
import com.delivery.driverauthservice.repository.VerificationCodeRepository;
import com.delivery.driverauthservice.security.CustomUserDetails;
import com.delivery.driverauthservice.security.JwtTokenProvider;
import com.delivery.driverauthservice.security.TokenBlacklistService;
import com.delivery.driverauthservice.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private final VehicleService vehicleService;
    private final DocumentService documentService;
    private final SequenceGenerator sequenceGenerator;
    private final DriverEventPublisher driverEventPublisher;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String VERIFICATION_CODE_CHARS = "0123456789";
    private static final int VERIFICATION_CODE_LENGTH = 6;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        // Authenticate with Spring Security - will work with username, email or phone
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getLoginIdentifier(), loginRequest.getPassword())
        );

        var userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Check if phone is verified
        if (!userDetails.isPhoneVerified()) {
            throw new VerificationException("Phone number not verified. Please verify your phone number.");
        }

        // Get driver from database
        DriverCredential driver = driverCredentialRepository.findById(userDetails.getDriverId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        updateDriverLoginInfo(driver, loginRequest);

        // Generate tokens
        String accessToken = tokenProvider.generateToken(driver);
        RefreshToken refreshToken = createRefreshToken(driver.getDriverId(), loginRequest.getDeviceId());

        // Get vehicle details and driver documents
        var authResponse = createAuthResponse(driver, accessToken, refreshToken.getToken());

        // Publish driver login event to driver management service
        publishDriverLoginEvent(driver, "ONLINE");

        return authResponse;
    }

    private void updateDriverLoginInfo(DriverCredential driver, LoginRequest loginRequest) {
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
    }

    @Override
    @Transactional
    public AuthResponse register(RegistrationRequest registrationRequest) {
        validateRegistrationData(registrationRequest);

        // Generate a temporary ID for the driver
        Long tempDriverId = sequenceGenerator.nextId();

        // Create driver credentials and vehicle
        DriverCredential driverCredential = createDriverCredential(registrationRequest, tempDriverId);
        Vehicle vehicle = createVehicle(registrationRequest, driverCredential);

        // Process document uploads
        var documentResult = processDocuments(driverCredential.getDriverId(), registrationRequest.getDocuments());
        List<DocumentDTO> uploadedDocuments = documentResult.getKey();
        Map<DocumentType, Long> documentIds = documentResult.getValue();

        // Send verification code
        try {
            sendVerificationCode(new SendVerificationRequest(registrationRequest.getPhoneNumber()));
        } catch (Exception e) {
            log.error("Failed to send verification code: {}", e.getMessage());
            // Continue with registration even if SMS fails
        }

        // Publish registration event
        publishRegistrationEvent(driverCredential, registrationRequest, documentIds);

        // Create vehicle DTO for response
        VehicleDTO vehicleDTO = createVehicleDTO(vehicle);

        // Return response without tokens (user needs to verify phone first)
        return AuthResponse.builder()
                .driverId(driverCredential.getDriverId())
                .username(driverCredential.getUsername())
                .firstName(driverCredential.getFirstName())
                .lastName(driverCredential.getLastName())
                .phoneNumber(driverCredential.getPhoneNumber())
                .email(driverCredential.getEmail())
                .phoneVerified(false)
                .registrationStatus(RegistrationStatus.PENDING.name())
                .roles(driverCredential.getRoles())
                .vehicleDetails(vehicleDTO)
                .documents(uploadedDocuments)
                .build();
    }

    private void validateRegistrationData(RegistrationRequest request) {
        // Check if username already exists
        if (driverCredentialRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        // Check if phone number already exists
        if (driverCredentialRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Phone number already registered");
        }

        // Check if email already exists (if provided)
        if (request.getEmail() != null && !request.getEmail().isEmpty() &&
                driverCredentialRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }

        // Validate required documents
        validateRequiredDocuments(request.getDocuments());
    }

    private DriverCredential createDriverCredential(RegistrationRequest request, Long driverId) {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_DRIVER");

        DriverCredential driverCredential = DriverCredential.builder()
                .driverId(driverId)
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(roles)
                .phoneVerified(false)
                .accountLocked(false)
                .registrationStatus(RegistrationStatus.PENDING)
                .failedLoginAttempts(0)
                .build();

        return driverCredentialRepository.save(driverCredential);
    }

    private Vehicle createVehicle(RegistrationRequest request, DriverCredential driver) {
        Vehicle vehicle = Vehicle.builder()
                .driver(driver)
                .brand(request.getVehicleBrand())
                .model(request.getVehicleModel())
                .year(request.getVehicleYear())
                .licensePlate(request.getLicensePlate())
                .color(request.getVehicleColor())
                .vehicleType(request.getVehicleType())
                .verified(false)
                .build();

        return vehicleRepository.save(vehicle);
    }

    private Map.Entry<List<DocumentDTO>, Map<DocumentType, Long>> processDocuments(
            Long driverId, Map<DocumentType, DocumentUploadMetadata> documents) {

        Map<DocumentType, Long> documentIds = new HashMap<>();
        List<DocumentDTO> uploadedDocuments = new ArrayList<>();

        if (documents != null && !documents.isEmpty()) {
            documents.forEach((documentType, metadata) -> {
                if (metadata == null || metadata.getBase64Image() == null ||
                        !isValidBase64(metadata.getBase64Image())) {
                    log.warn("Skipping invalid document data for type: {}", documentType);
                    return;
                }

                DocumentUploadRequest docRequest = new DocumentUploadRequest();
                docRequest.setDriverId(driverId);
                docRequest.setDocumentType(documentType);
                docRequest.setBase64Image(metadata.getBase64Image());
                docRequest.setFileName(metadata.getFileName() != null ?
                        metadata.getFileName() : generateDefaultFileName(documentType));
                docRequest.setContentType(metadata.getContentType() != null ?
                        metadata.getContentType() : "image/jpeg");
                docRequest.setExpiryDate(metadata.getExpiryDate());

                try {
                    DocumentDTO document = documentService.uploadDocumentBase64(docRequest);
                    uploadedDocuments.add(document);
                    documentIds.put(documentType, document.getId());
                } catch (Exception e) {
                    log.error("Error uploading document of type {}: {}", documentType, e.getMessage());
                }
            });
        }

        return Map.entry(uploadedDocuments, documentIds);
    }

    private void publishRegistrationEvent(DriverCredential driver, RegistrationRequest request,
                                          Map<DocumentType, Long> documentIds) {
        DriverRegistrationEvent event = DriverRegistrationEvent.builder()
                .tempDriverId(driver.getDriverId())
                .username(request.getUsername())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .licenseNumber(request.getLicenseNumber())
                .vehicleType(request.getVehicleType())
                .vehicleBrand(request.getVehicleBrand())
                .vehicleModel(request.getVehicleModel())
                .vehicleYear(request.getVehicleYear())
                .licensePlate(request.getLicensePlate())
                .vehicleColor(request.getVehicleColor())
                .documentIds(documentIds)
                .build();

        driverEventPublisher.publishDriverRegistration(event);
    }

    private boolean isValidBase64(String base64String) {
        try {
            // Remove any data URL prefix (like "data:image/jpeg;base64,")
            String strToValidate = base64String;
            if (base64String.contains(",")) {
                String[] parts = base64String.split(",");
                if (parts.length > 1) {
                    strToValidate = parts[1];
                }
            }

            // Try to decode - this will throw if invalid
            Base64.getDecoder().decode(strToValidate);
            return true;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid Base64 string: {}", e.getMessage());
            return false;
        }
    }

    private void validateRequiredDocuments(Map<DocumentType, DocumentUploadMetadata> documents) {
        // Define the required document types
        Set<DocumentType> requiredDocTypes = Set.of(
                DocumentType.DRIVING_LICENSE,
                DocumentType.VEHICLE_INSURANCE,
                DocumentType.VEHICLE_REGISTRATION
        );

        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("Required documents are missing");
        }

        // Check if all required document types are present
        for (DocumentType requiredType : requiredDocTypes) {
            if (!documents.containsKey(requiredType) ||
                    documents.get(requiredType) == null ||
                    documents.get(requiredType).getBase64Image() == null ||
                    documents.get(requiredType).getBase64Image().isEmpty()) {
                throw new IllegalArgumentException("Required document missing: " + requiredType);
            }
        }
    }

    private String generateDefaultFileName(DocumentType documentType) {
        return documentType.name().toLowerCase().replace('_', '-') + "-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".jpg";
    }

    @Override
    @Transactional
    public boolean sendVerificationCode(SendVerificationRequest request) {
        // Generate a 6-digit code using SecureRandom
        String code = generateSecureRandomCode();

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

    private String generateSecureRandomCode() {
        StringBuilder sb = new StringBuilder(VERIFICATION_CODE_LENGTH);
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(VERIFICATION_CODE_CHARS.length());
            sb.append(VERIFICATION_CODE_CHARS.charAt(randomIndex));
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public boolean verifyPhoneNumber(PhoneVerificationRequest request) {
        // Find active verification code
        var verificationCodeOpt = verificationCodeRepository.findActiveCodeByPhoneNumber(
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
        var driverOpt = driverCredentialRepository.findByPhoneNumber(request.getPhoneNumber());
        if (driverOpt.isPresent()) {
            DriverCredential driver = driverOpt.get();
            driver.setPhoneVerified(true);
            driverCredentialRepository.save(driver);

            // Publish phone verification event to driver management service
            publishDriverVerificationEvent(driver);
        }

        return true;
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String requestRefreshToken = refreshTokenRequest.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));

        // Check if token is expired or device mismatch
        validateRefreshToken(refreshToken, refreshTokenRequest.getDeviceId());

        // Get driver
        DriverCredential driver = driverCredentialRepository.findById(refreshToken.getDriverId())
                .orElseThrow(() -> new TokenRefreshException("User not found"));

        // Generate new tokens
        String accessToken = tokenProvider.generateToken(driver);
        RefreshToken newRefreshToken = createRefreshToken(driver.getDriverId(), refreshToken.getDeviceId());

        // Delete old refresh token
        refreshTokenRepository.delete(refreshToken);

        // Create and return the authentication response
        return createAuthResponse(driver, accessToken, newRefreshToken.getToken());
    }

    private void validateRefreshToken(RefreshToken refreshToken, String deviceId) {
        // Check if token is expired
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token expired. Please login again.");
        }

        // Check device ID if provided
        if (deviceId != null && !deviceId.equals(refreshToken.getDeviceId())) {
            throw new TokenRefreshException("Invalid device. Please login again.");
        }
    }

    private AuthResponse createAuthResponse(DriverCredential driver, String accessToken, String refreshToken) {
        // Get vehicle details
        VehicleDTO vehicleDetails = vehicleService.getVehicleByDriverId(driver.getDriverId());

        // Get driver documents
        List<DocumentDTO> documents = documentService.getDriverDocuments(driver.getDriverId());

        return AuthResponse.builder()
                .driverId(driver.getDriverId())
                .username(driver.getUsername())
                .firstName(driver.getFirstName())
                .lastName(driver.getLastName())
                .phoneNumber(driver.getPhoneNumber())
                .email(driver.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenProvider.getTokenExpirationInSeconds())
                .roles(driver.getRoles())
                .tokenType("Bearer")
                .phoneVerified(driver.isPhoneVerified())
                .registrationStatus(driver.getRegistrationStatus().name())
                .vehicleDetails(vehicleDetails)
                .documents(documents)
                .build();
    }

    private VehicleDTO createVehicleDTO(Vehicle vehicle) {
        return VehicleDTO.builder()
                .id(vehicle.getId())
                .driverId(vehicle.getDriver().getDriverId())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .licensePlate(vehicle.getLicensePlate())
                .color(vehicle.getColor())
                .vehicleType(vehicle.getVehicleType())
                .verified(vehicle.getVerified())
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
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails principal) {
            Long driverId = principal.getDriverId();

            // Find driver
            driverCredentialRepository.findById(driverId).ifPresent(driver -> {
                // Publish driver logout event to driver management service
                publishDriverLoginEvent(driver, "OFFLINE");
            });
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

    /**
     * Publishes a driver login/logout event to the driver management service
     */
    private void publishDriverLoginEvent(DriverCredential driver, String status) {
        try {
            // Create a login status event and publish it
            Map<String, Object> event = new HashMap<>();
            event.put("driverId", driver.getDriverId());
            event.put("status", status);
            event.put("timestamp", LocalDateTime.now());

            // Use the driver event publisher to send this event
            driverEventPublisher.publishDriverStatusUpdate(event);

            log.info("Published driver status update: {}", status);
        } catch (Exception e) {
            log.error("Failed to publish driver status update: {}", e.getMessage());
            // Continue anyway - don't block authentication for this
        }
    }

    /**
     * Publishes a driver verification event to the driver management service
     */
    private void publishDriverVerificationEvent(DriverCredential driver) {
        try {
            // Create an event with verification info
            Map<String, Object> event = new HashMap<>();
            event.put("driverId", driver.getDriverId());
            event.put("phoneVerified", true);
            event.put("timestamp", LocalDateTime.now());

            // Use the driver event publisher to send this event
            driverEventPublisher.publishDriverVerification(event);

            log.info("Published driver verification event for driver ID: {}", driver.getDriverId());
        } catch (Exception e) {
            log.error("Failed to publish driver verification event: {}", e.getMessage());
            // Continue anyway - don't block verification process for this
        }
    }
}