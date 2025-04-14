package com.delivery.driverauthservice.service;

import com.delivery.driverauthservice.dto.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse register(RegistrationRequest registrationRequest);

    AuthResponse registerAdmin(AdminRegistrationRequest registrationRequest);

    boolean sendVerificationCode(SendVerificationRequest request);

    boolean verifyPhoneNumber(PhoneVerificationRequest request);

    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    boolean resetPassword(PasswordResetRequest resetRequest);

    boolean logout(Authentication authentication, String token);
}