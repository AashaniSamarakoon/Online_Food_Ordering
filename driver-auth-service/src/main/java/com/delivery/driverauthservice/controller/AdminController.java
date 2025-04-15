package com.delivery.driverauthservice.controller;

import com.delivery.driverauthservice.dto.AdminRegistrationRequest;
import com.delivery.driverauthservice.dto.AuthResponse;
import com.delivery.driverauthservice.dto.DriverDetailsDTO;
import com.delivery.driverauthservice.dto.DriverDocumentVerificationRequest;
import com.delivery.driverauthservice.dto.DriverVehicleVerificationRequest;
import com.delivery.driverauthservice.service.AuthService;
import com.delivery.driverauthservice.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AuthServiceImpl authServiceImpl;
    private final AuthService authService;

    // Make sure the endpoint is properly mapped
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody AdminRegistrationRequest request) {
        // Check for required fields manually if @Valid isn't working properly
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getFirstName() == null || request.getFirstName().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        AuthResponse response = authService.registerAdmin(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/verify-document")
    public ResponseEntity<String> verifyDriverDocument(@RequestBody DriverDocumentVerificationRequest request) {
        if (request.getDocumentId() == null) {
            return ResponseEntity.badRequest().body("Document ID is required");
        }

        if (request.getValid() == null) {
            return ResponseEntity.badRequest().body("Validation status (isValid) is required");
        }

        log.info("Verifying document: {}, isValid: {}, remarks: {}",
                request.getDocumentId(), request.isValid(), request.getRemarks());

        try {
            authServiceImpl.verifyDocument(
                    request.getDocumentId(),
                    request.isValid(), // Using our convenience method
                    request.getRemarks() != null ? request.getRemarks() : ""
            );
            return ResponseEntity.ok("Document verification status updated successfully");
        } catch (Exception e) {
            log.error("Error verifying document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/verify-vehicle")
    public ResponseEntity<String> verifyDriverVehicle(@RequestBody DriverVehicleVerificationRequest request) {
        if (request.getVehicleId() == null) {
            return ResponseEntity.badRequest().body("Vehicle ID is required");
        }

        if (request.getValid() == null) {
            return ResponseEntity.badRequest().body("Validation status (isValid) is required");
        }

        log.info("Verifying vehicle: {}, isValid: {}", request.getVehicleId(), request.isValid());

        try {
            authServiceImpl.verifyVehicle(request.getVehicleId(), request.isValid());
            return ResponseEntity.ok("Vehicle verification status updated successfully");
        } catch (Exception e) {
            log.error("Error verifying vehicle: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/complete-registration/{driverId}")
    public ResponseEntity<DriverDetailsDTO> completeDriverRegistration(@PathVariable Long driverId) {
        DriverDetailsDTO driverDetails = authServiceImpl.completeDriverRegistration(driverId);
        return ResponseEntity.ok(driverDetails);
    }
}