package com.delivery.driverauthservice.controller;

import com.delivery.driverauthservice.dto.*;
import com.delivery.driverauthservice.security.JwtTokenProvider;
import com.delivery.driverauthservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
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