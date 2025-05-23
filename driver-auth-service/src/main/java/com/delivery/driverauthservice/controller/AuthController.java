package com.delivery.driverauthservice.controller;

import com.delivery.driverauthservice.dto.*;
import com.delivery.driverauthservice.model.DocumentType;
import com.delivery.driverauthservice.security.JwtTokenProvider;
import com.delivery.driverauthservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

@RestController
@RequestMapping("/api/driver/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        return new ResponseEntity<>(authService.register(registrationRequest), HttpStatus.CREATED);
    }

    @PostMapping(value = "/register-with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> registerWithFiles(
            @RequestPart("driverData") @Valid RegistrationRequest registrationRequest,
            @RequestPart(value = "profilePhoto", required = false) MultipartFile profilePhoto,
            @RequestPart(value = "drivingLicense", required = true) MultipartFile drivingLicense,
            @RequestPart(value = "vehicleInsurance", required = true) MultipartFile vehicleInsurance,
            @RequestPart(value = "revenueLicense", required = false) MultipartFile revenueLicense,
            @RequestPart(value = "vehicleRegistration", required = true) MultipartFile vehicleRegistration) {

        try {
            // Initialize documents map if null
            if (registrationRequest.getDocuments() == null) {
                registrationRequest.setDocuments(new HashMap<>());
            }

            // Process profile photo if provided
            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                addDocumentToRequest(registrationRequest, DocumentType.PROFILE_PHOTO, profilePhoto);
            }

            // Process required documents
            addDocumentToRequest(registrationRequest, DocumentType.DRIVING_LICENSE, drivingLicense);
            addDocumentToRequest(registrationRequest, DocumentType.VEHICLE_INSURANCE, vehicleInsurance);
            addDocumentToRequest(registrationRequest, DocumentType.VEHICLE_REGISTRATION, vehicleRegistration);

            // Process optional revenue license if provided
            if (revenueLicense != null && !revenueLicense.isEmpty()) {
                addDocumentToRequest(registrationRequest, DocumentType.REVENUE_LICENSE, revenueLicense);
            }

            return new ResponseEntity<>(authService.register(registrationRequest), HttpStatus.CREATED);
        } catch (IOException e) {
            throw new RuntimeException("Error processing document files", e);
        }
    }

    private void addDocumentToRequest(RegistrationRequest request, DocumentType type, MultipartFile file) throws IOException {
        DocumentUploadMetadata metadata = DocumentUploadMetadata.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .base64Image(Base64.getEncoder().encodeToString(file.getBytes()))
                .build();

        request.getDocuments().put(type, metadata);
    }

    @PostMapping("/send-verification")
    public ResponseEntity<ApiResponse> sendVerificationCode(@Valid @RequestBody SendVerificationRequest request) {
        boolean sent = authService.sendVerificationCode(request);
        return ResponseEntity.ok(new ApiResponse(sent,
                sent ? "Verification code sent" : "Failed to send verification code"));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyPhoneNumber(@Valid @RequestBody PhoneVerificationRequest request) {
        boolean verified = authService.verifyPhoneNumber(request);
        return ResponseEntity.ok(new ApiResponse(verified,
                verified ? "Phone number verified successfully" : "Verification failed"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        boolean reset = authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse(reset,
                reset ? "Password reset successfully" : "Password reset failed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(Authentication authentication, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        boolean success = authService.logout(authentication, token);
        return ResponseEntity.ok(new ApiResponse(success, "Logged out successfully"));
    }


}