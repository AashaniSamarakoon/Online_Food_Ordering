package com.delivery.driverauthservice.service.impl;

import com.delivery.driverauthservice.client.DriverServiceClient;
import com.delivery.driverauthservice.dto.*;
import com.delivery.driverauthservice.exception.InvalidCredentialsException;
import com.delivery.driverauthservice.exception.ResourceAlreadyExistsException;
import com.delivery.driverauthservice.exception.TokenRefreshException;
import com.delivery.driverauthservice.exception.VerificationException;
import com.delivery.driverauthservice.messaging.DriverEventPublisher;
import com.delivery.driverauthservice.messaging.DriverRegistrationEvent;
import com.delivery.driverauthservice.model.*;
import com.delivery.driverauthservice.repository.*;
import com.delivery.driverauthservice.security.CustomUserDetails;
import com.delivery.driverauthservice.security.JwtTokenProvider;
import com.delivery.driverauthservice.security.TokenBlacklistService;
import com.delivery.driverauthservice.service.*;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private final DocumentService documentService;
    private final SequenceGenerator sequenceGenerator;
    private final DriverEventPublisher driverEventPublisher;
    private final DriverDocumentRepository driverDocumentRepository;


    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        // Authenticate with Spring Security - will work with username, email or phone
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getLoginIdentifier(), loginRequest.getPassword())
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

        // Get vehicle details
        VehicleDTO vehicleDetails = vehicleService.getVehicleByDriverId(driver.getDriverId());

        // Get driver documents
        List<DocumentDTO> documents = documentService.getDriverDocuments(driver.getDriverId());

        // Create and return the authentication response
        return AuthResponse.builder()
                .driverId(driver.getDriverId())
                .username(driver.getUsername())
                .firstName(driver.getFirstName())
                .lastName(driver.getLastName())
                .phoneNumber(driver.getPhoneNumber())
                .email(driver.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(tokenProvider.getTokenExpirationInSeconds())
                .roles(driver.getRoles())
                .tokenType("Bearer")
                .phoneVerified(driver.isPhoneVerified())
                .registrationStatus(driver.getRegistrationStatus().name())
                .vehicleDetails(vehicleDetails)
                .documents(documents)
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

        // Check if email already exists (if provided)
        if (registrationRequest.getEmail() != null && !registrationRequest.getEmail().isEmpty() &&
                driverCredentialRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }

        // Validate required documents
        validateRequiredDocuments(registrationRequest.getDocuments());

        // Generate a temporary ID for the driver
        Long tempDriverId = sequenceGenerator.nextId();

        // Create driver credentials first
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_DRIVER");

        DriverCredential driverCredential = DriverCredential.builder()
                .driverId(tempDriverId)
                .username(registrationRequest.getUsername())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .email(registrationRequest.getEmail())
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .roles(roles)
                .phoneVerified(false)
                .accountLocked(false)
                .registrationStatus(RegistrationStatus.PENDING_VERIFICATION) // Changed to PENDING_VERIFICATION
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
                .verified(false) // Document needs to be verified
                .build();

        vehicleRepository.save(vehicle);

        // Process document uploads and store them locally
        Map<DocumentType, Long> documentIds = new HashMap<>();
        List<DocumentDTO> uploadedDocuments = new ArrayList<>();

        if (registrationRequest.getDocuments() != null && !registrationRequest.getDocuments().isEmpty()) {
            for (Map.Entry<DocumentType, DocumentUploadMetadata> entry : registrationRequest.getDocuments().entrySet()) {
                DocumentType documentType = entry.getKey();
                DocumentUploadMetadata metadata = entry.getValue();

                log.info("Processing document of type: {}", documentType);

                if (metadata == null) {
                    log.warn("Metadata is null for document type: {}", documentType);
                    continue;
                }

                if (metadata.getBase64Image() == null) {
                    log.warn("Base64 image is null for document type: {}", documentType);
                    continue;
                }

                if (!isValidBase64(metadata.getBase64Image())) {
                    log.warn("Invalid base64 data for document type: {}", documentType);
                    continue;
                }

                try {
                    DocumentUploadRequest docRequest = new DocumentUploadRequest();
                    docRequest.setDriverId(driverCredential.getDriverId());
                    docRequest.setDocumentType(documentType);
                    docRequest.setBase64Image(metadata.getBase64Image());
                    docRequest.setFileName(metadata.getFileName() != null ?
                            metadata.getFileName() : generateDefaultFileName(documentType));
                    docRequest.setContentType(metadata.getContentType() != null ?
                            metadata.getContentType() : "image/jpeg");
                    docRequest.setExpiryDate(metadata.getExpiryDate());
                    docRequest.setVerified(false);

                    log.info("Uploading document for driver: {}, type: {}, filename: {}",
                            driverCredential.getDriverId(), documentType, docRequest.getFileName());

                    DocumentDTO document = documentService.uploadDocumentBase64(docRequest);
                    uploadedDocuments.add(document);
                    documentIds.put(documentType, document.getId());

                    log.info("Successfully uploaded document with ID: {}", document.getId());
                } catch (Exception e) {
                    log.error("Error uploading document of type {}: {}", documentType, e.getMessage(), e);
                    // Continue with other documents even if one fails
                }
            }
        }

        // Send verification code for phone verification
        try {
            sendVerificationCode(new SendVerificationRequest(registrationRequest.getPhoneNumber()));
        } catch (Exception e) {
            log.error("Failed to send verification code: {}", e.getMessage());
            // Continue with registration even if SMS fails
        }

        // Create vehicle DTO for response
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

        // Return response without tokens (user needs to verify phone and documents first)
        return AuthResponse.builder()
                .driverId(driverCredential.getDriverId())
                .username(driverCredential.getUsername())
                .firstName(driverCredential.getFirstName())
                .lastName(driverCredential.getLastName())
                .phoneNumber(driverCredential.getPhoneNumber())
                .email(driverCredential.getEmail())
                .phoneVerified(false)
                .registrationStatus(RegistrationStatus.PENDING_VERIFICATION.name())
                .roles(roles)
                .vehicleDetails(vehicleDTO)
                .documents(uploadedDocuments)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse registerAdmin(AdminRegistrationRequest registrationRequest) {
        // Log the incoming request
        log.info("Registering new admin: {}", registrationRequest.getUsername());

        // Check if username already exists
        if (driverCredentialRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        // Check if email already exists (if provided)
        if (registrationRequest.getEmail() != null && !registrationRequest.getEmail().isEmpty() &&
                driverCredentialRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }

        // Generate an ID for the admin
        Long adminId = sequenceGenerator.nextId();

        // Create admin credentials
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");

        DriverCredential adminCredential = DriverCredential.builder()
                .driverId(adminId)
                .username(registrationRequest.getUsername())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .email(registrationRequest.getEmail())
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .roles(roles)
                .phoneVerified(true)  // Admins don't need phone verification
                .accountLocked(false)
                .registrationStatus(RegistrationStatus.COMPLETED)  // Admin accounts are immediately active
                .failedLoginAttempts(0)
                .build();

        driverCredentialRepository.save(adminCredential);

        // Return response without tokens (for security, tokens are only generated on login)
        return AuthResponse.builder()
                .driverId(adminCredential.getDriverId())
                .username(adminCredential.getUsername())
                .firstName(adminCredential.getFirstName())
                .lastName(adminCredential.getLastName())
                .phoneNumber(adminCredential.getPhoneNumber())
                .email(adminCredential.getEmail())
                .phoneVerified(true)
                .registrationStatus(RegistrationStatus.COMPLETED.name())
                .roles(roles)
                .build();
    }

    /**
     * Method to register driver with management service after verification is complete
     * This will be called by admin after verifying documents and vehicle
     * Now with RabbitMQ fallback for resilience using existing publisher
     */
    @Transactional
    public DriverDetailsDTO completeDriverRegistration(Long driverId) {
        DriverCredential driver = driverCredentialRepository.findByDriverId(driverId)
                .orElseThrow(() -> new ResourceAlreadyExistsException("Driver not found"));

        // Check if documents are verified
        boolean allDocumentsVerified = documentService.areAllDocumentsVerified(driverId);

        if (!allDocumentsVerified) {
            throw new VerificationException("All documents must be verified before completing registration");
        }

        // Check if vehicle is verified
        Vehicle vehicle = vehicleRepository.findByDriverId(driverId)
                .orElseThrow(() -> new VerificationException("Vehicle details not found"));

        if (!vehicle.getVerified()) {
            throw new VerificationException("Vehicle must be verified before completing registration");
        }

        // Check if phone is verified
        if (!driver.isPhoneVerified()) {
            throw new VerificationException("Phone number must be verified before completing registration");
        }

        try {
            // Create registration DTO - KEEP THIS UNCHANGED as requested
            DriverRegistrationDTO dto = DriverRegistrationDTO.builder()
                    .driverId(driver.getDriverId()) // Use .id instead of .driverId
                    .username(driver.getUsername())
                    .firstName(driver.getFirstName())
                    .lastName(driver.getLastName())
                    .phoneNumber(driver.getPhoneNumber())
                    .email(driver.getEmail())
                    .licenseNumber(vehicle.getLicensePlate()) // Using license plate as a fallback for license number
                    .vehicleType(vehicle.getVehicleType())
                    .vehicleBrand(vehicle.getBrand())
                    .vehicleModel(vehicle.getModel())
                    .vehicleYear(vehicle.getYear())
                    .licensePlate(vehicle.getLicensePlate())
                    .vehicleColor(vehicle.getColor())
                    .latitude(0.0)  // Default until driver updates
                    .longitude(0.0) // Default until driver updates
                    .build();

            // Register with driver service
            DriverDetailsDTO driverDetails = driverServiceClient.registerDriver(dto);

            // Update driver status to COMPLETED
            driver.setRegistrationStatus(RegistrationStatus.COMPLETED);
            driverCredentialRepository.save(driver);

            log.info("Successfully completed registration for driver: {}", driver.getUsername());

            return driverDetails;
        } catch (Exception e) {
            log.error("Failed to complete registration for driver {} through direct API: {}",
                    driver.getUsername(), e.getMessage());

            // Fallback to RabbitMQ using the existing DriverEventPublisher
            try {
                // Fetch document IDs for this driver
                List<DriverDocument> documents = driverDocumentRepository.findByDriverDriverId(driverId);
                Map<DocumentType, Long> documentIds = documents.stream()
                        .collect(Collectors.toMap(DriverDocument::getDocumentType, DriverDocument::getId));

                // Create event for asynchronous processing
                DriverRegistrationEvent event = DriverRegistrationEvent.builder()
                        .tempDriverId(driver.getDriverId())
                        .username(driver.getUsername())
                        .email(driver.getEmail())
                        .phoneNumber(driver.getPhoneNumber())
                        .firstName(driver.getFirstName())
                        .lastName(driver.getLastName())
                        .licenseNumber(vehicle.getLicensePlate())
                        .vehicleType(vehicle.getVehicleType())
                        .vehicleBrand(vehicle.getBrand())
                        .vehicleModel(vehicle.getModel())
                        .vehicleYear(vehicle.getYear())
                        .licensePlate(vehicle.getLicensePlate())
                        .vehicleColor(vehicle.getColor())
                        .documentIds(documentIds)
                        .build();

                // Mark as pending for asynchronous processing
                driver.setRegistrationStatus(RegistrationStatus.PENDING);
                driverCredentialRepository.save(driver);

                // Use the existing publisher to publish the event
                driverEventPublisher.publishDriverRegistration(event);

                log.info("Registration event published for driver: {} with ID: {}",
                        driver.getUsername(), driver.getDriverId());

                return DriverDetailsDTO.builder()
                        .id(driver.getDriverId())
                        .username(driver.getUsername())
                        .firstName(driver.getFirstName())
                        .lastName(driver.getLastName())
                        .status("PENDING")
                        .message("Registration in progress. You'll be notified when completed.")
                        .build();

            } catch (Exception mqException) {
                // Both direct API and RabbitMQ failed
                log.error("Failed to publish driver registration event: {}", mqException.getMessage());
                // Re-throw the original exception
                throw new RuntimeException("Failed to complete driver registration: " + e.getMessage());
            }
        }
    }

    // Update the verifyDocument method to include notes parameter
    @Transactional
    public boolean verifyDocument(Long documentId, boolean isValid, String remarks) {
        log.info("AuthServiceImpl - Verifying document ID: {}, isValid: {}, remarks: {}",
                documentId, isValid, remarks);

        boolean success = documentService.verifyDocument(documentId, isValid, remarks);

        if (!success) {
            throw new ResourceAlreadyExistsException("Document not found with ID: " + documentId);
        }
        return success;
    }


    /**
     * Verify a driver vehicle
     */
    /**
     * Verify a driver vehicle
     */
    @Transactional
    public void verifyVehicle(Long vehicleId, boolean isValid) {
        log.info("Verifying vehicle ID: {}, isValid: {}", vehicleId, isValid);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceAlreadyExistsException("Vehicle not found with ID: " + vehicleId));

        vehicle.setVerified(isValid);
        vehicleRepository.saveAndFlush(vehicle); // Use saveAndFlush to ensure immediate persistence

        log.info("Vehicle verification updated. Vehicle ID: {}, New verification status: {}",
                vehicle.getId(), vehicle.getVerified());
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

        // Get vehicle details
        VehicleDTO vehicleDetails = vehicleService.getVehicleByDriverId(driver.getDriverId());

        // Get driver documents
        List<DocumentDTO> documents = documentService.getDriverDocuments(driver.getDriverId());

        // Create and return the authentication response
        return AuthResponse.builder()
                .driverId(driver.getDriverId())
                .username(driver.getUsername())
                .firstName(driver.getFirstName())
                .lastName(driver.getLastName())
                .phoneNumber(driver.getPhoneNumber())
                .email(driver.getEmail())
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .expiresIn(tokenProvider.getTokenExpirationInSeconds())
                .roles(driver.getRoles())
                .tokenType("Bearer")
                .phoneVerified(driver.isPhoneVerified())
                .registrationStatus(driver.getRegistrationStatus().name())
                .vehicleDetails(vehicleDetails)
                .documents(documents)
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
        // Just blacklist the token, no need to update driver status
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