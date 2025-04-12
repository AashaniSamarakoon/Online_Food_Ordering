package com.delivery.driverauthservice.service;

import com.delivery.driverauthservice.dto.*;
import org.springframework.security.core.Authentication;

public interface AuthService {

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse register(RegistrationRequest registrationRequest);

    boolean sendVerificationCode(SendVerificationRequest request);

    boolean verifyPhoneNumber(PhoneVerificationRequest request);

    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    boolean resetPassword(PasswordResetRequest resetRequest);

    boolean logout(Authentication authentication, String token);
}