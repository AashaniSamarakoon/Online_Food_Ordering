////package com.example.restaurantauth.service;
////
////import com.example.restaurantauth.config.JwtService;
////import com.example.restaurantauth.dto.*;
////import com.example.restaurantauth.exception.AccountNotVerifiedException;
////import com.example.restaurantauth.model.RestaurantAdmin;
////import com.example.restaurantauth.repository.RestaurantAdminRepository;
////import jakarta.annotation.PostConstruct;
////import lombok.RequiredArgsConstructor;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.security.authentication.AuthenticationManager;
////import org.springframework.security.authentication.BadCredentialsException;
////import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
////import org.springframework.security.core.userdetails.UsernameNotFoundException;
////import org.springframework.security.crypto.password.PasswordEncoder;
////import org.springframework.stereotype.Service;
////import org.springframework.transaction.annotation.Transactional;
////
////@Slf4j
////@Service
////@RequiredArgsConstructor
////public class AuthService {
////    private final RestaurantAdminRepository repository;
////    private final PasswordEncoder passwordEncoder;
////    private final JwtService jwtService;
////    private final AuthenticationManager authenticationManager;
////
////    @PostConstruct
////    @Transactional
////    public void initSuperAdmin() {
////        try {
////            if (!repository.existsByEmail("superadmin@example.com")) {
////                RestaurantAdmin superAdmin = RestaurantAdmin.builder()
////                        .email("superadmin@example.com")
////                        .password(passwordEncoder.encode("superadmin123"))
////                        .restaurantName("System Administration")
////                        .ownerName("System Admin")
////                        .nic("000000000V")
////                        .phone("0000000000")
////                        .address("System Address")
////                        .latitude(0.0)
////                        .longitude(0.0)
////                        .bankAccountOwner("System Admin")
////                        .bankName("System Bank")
////                        .branchName("System Branch")
////                        .accountNumber("0000000000")
////                        .role(RestaurantAdmin.Role.SUPER_ADMIN)
////                        .isVerified(true)
////                        .build();
////                repository.save(superAdmin);
////                log.info("Super admin account initialized successfully");
////            }
////        } catch (Exception e) {
////            log.error("Failed to initialize super admin account", e);
////        }
////    }
////
////    @Transactional
////    public RegisterResponse register(RegisterRequest request) {
////        if (repository.existsByEmail(request.getEmail())) {
////            throw new RuntimeException("Email already in use");
////        }
////        if (repository.existsByNic(request.getNic())) {
////            throw new RuntimeException("NIC already registered");
////        }
////        if (repository.existsByAccountNumber(request.getAccountNumber())) {
////            throw new RuntimeException("Bank account already registered");
////        }
////
////        var admin = RestaurantAdmin.builder()
////                .email(request.getEmail())
////                .password(passwordEncoder.encode(request.getPassword()))
////                .restaurantName(request.getRestaurantName())
////                .ownerName(request.getOwnerName())
////                .nic(request.getNic())
////                .phone(request.getPhone())
////                .address(request.getAddress())
////                .latitude(request.getLatitude())
////                .longitude(request.getLongitude())
////                .bankAccountOwner(request.getBankAccountOwner())
////                .bankName(request.getBankName())
////                .branchName(request.getBranchName())
////                .accountNumber(request.getAccountNumber())
////                .role(RestaurantAdmin.Role.RESTAURANT_ADMIN)
////                .isVerified(false)
////                .build();
////
////        repository.save(admin);
////        log.info("New admin registered: {}", request.getEmail());
////
////        return RegisterResponse.builder()
////                .email(admin.getEmail())
////                .restaurantName(admin.getRestaurantName())
////                .message("Registration successful! Please wait for verification.")
////                .build();
////    }
////
////    // In your AuthService.java
////    public AuthResponse authenticate(AuthRequest request) {
////        RestaurantAdmin admin = repository.findByEmail(request.getEmail())
////                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
////
////        // Debug: Log actual role from database
////        log.info("Authenticating {} with role: {}", admin.getEmail(), admin.getRole());
////
////        // Force new token generation with current DB state
////        String jwtToken = jwtService.generateToken(admin);
////
////        return AuthResponse.builder()
////                .token(jwtToken)
////                .role(admin.getRole().name()) // Get directly from DB
////                .build();
////    }
////}
//
//package com.example.restaurantauth.service;
//
//import com.example.restaurantauth.config.JwtService;
//import com.example.restaurantauth.dto.*;
//import com.example.restaurantauth.exception.AccountNotVerifiedException;
//import com.example.restaurantauth.model.RestaurantAdmin;
//import com.example.restaurantauth.repository.RestaurantAdminRepository;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//    private final RestaurantAdminRepository repository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtService jwtService;
//    private final AuthenticationManager authenticationManager;
//
//    @PostConstruct
//    @Transactional
//    public void initSuperAdmin() {
//        try {
//            // Check if super admin exists with this account number before creating
//            if (!repository.existsByAccountNumber("0000000000")) {
//                // Only create if it doesn't exist
//                RestaurantAdmin superAdmin = RestaurantAdmin.builder()
//                        .email("ashanisamarakoon36@gmail.com")
//                        .password(passwordEncoder.encode("ixfyqkskuswmhkok"))
//                        .role(RestaurantAdmin.Role.valueOf("SUPER_ADMIN"))
//                        .ownerName("Super Admin")
//                        .restaurantName("System Admin")
//                        .phone("0000000000")
//                        .nic("0000000000V")
//                        .address("System Address")
//                        .accountNumber("0000000000")
//                        .bankName("System Bank")
//                        .branchName("System Branch")
//                        .bankAccountOwner("System")
//                        .isVerified(true)
//                        .build();
//
//                repository.save(superAdmin);
//                log.info("Super admin account initialized successfully");
//            } else {
//                log.info("Super admin account already exists, skipping initialization");
//            }
//        } catch (Exception e) {
//            log.error("Failed to initialize super admin account", e);
//        }
//    }
//
//    @Transactional
//    public RegisterResponse register(RegisterRequest request) {
//        if (repository.existsByEmail(request.getEmail())) {
//            throw new RuntimeException("Email already in use");
//        }
//        if (repository.existsByNic(request.getNic())) {
//            throw new RuntimeException("NIC already registered");
//        }
//        if (repository.existsByAccountNumber(request.getAccountNumber())) {
//            throw new RuntimeException("Bank account already registered");
//        }
//
//        var admin = RestaurantAdmin.builder()
//                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .restaurantName(request.getRestaurantName())
//                .ownerName(request.getOwnerName())
//                .nic(request.getNic())
//                .phone(request.getPhone())
//                .address(request.getAddress())
//                .latitude(request.getLatitude())
//                .longitude(request.getLongitude())
//                .bankAccountOwner(request.getBankAccountOwner())
//                .bankName(request.getBankName())
//                .branchName(request.getBranchName())
//                .accountNumber(request.getAccountNumber())
//                .role(RestaurantAdmin.Role.RESTAURANT_ADMIN)
//                .isVerified(false)
//                .build();
//
//        repository.save(admin);
//        log.info("New admin registered: {}", request.getEmail());
//
//        return RegisterResponse.builder()
//                .email(admin.getEmail())
//                .restaurantName(admin.getRestaurantName())
//                .message("Registration successful! Please wait for verification.")
//                .build();
//    }
//
//    public AuthResponse authenticate(AuthRequest request) {
//        try {
//            // First verify credentials
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.getEmail(),
//                            request.getPassword()
//                    )
//            );
//
//            // If authentication passed, get the user
//            RestaurantAdmin admin = repository.findByEmail(request.getEmail())
//                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//            // Check verification for restaurant admins only
//            if (admin.getRole() == RestaurantAdmin.Role.RESTAURANT_ADMIN && !admin.isVerified()) {
//                log.warn("Login attempt by unverified user: {}", admin.getEmail());
//                throw new AccountNotVerifiedException(
//                        "Your account has not been verified yet. Please wait for admin verification."
//                );
//            }
//
//            log.info("Authenticated user: {} with role: {}", admin.getEmail(), admin.getRole());
//
//            // Generate token for verified user
//            String jwtToken = jwtService.generateToken(admin);
//
//            return AuthResponse.builder()
//                    .token(jwtToken)
//                    .role(admin.getRole().name())
//                    .email(admin.getEmail())
//                    .restaurantName(admin.getRestaurantName())
//                    .isVerified(admin.isVerified())
//                    .build();
//
//        } catch (BadCredentialsException e) {
//            log.warn("Authentication failed: Invalid credentials for {}", request.getEmail());
//            throw new BadCredentialsException("Invalid email or password");
//        }
//    }
//
//    public Optional<RestaurantAdmin> findByEmail(String email) {
//        return repository.findByEmail(email);
//    }
//}

package com.example.restaurantauth.service;

import com.example.restaurantauth.config.JwtService;
import com.example.restaurantauth.dto.*;
import com.example.restaurantauth.exception.AccountNotVerifiedException;
import com.example.restaurantauth.model.RestaurantAdmin;
import com.example.restaurantauth.model.VerificationToken;
import com.example.restaurantauth.repository.RestaurantAdminRepository;
import com.example.restaurantauth.repository.VerificationTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final RestaurantAdminRepository repository;
    private final VerificationTokenRepository tokenRepository; // Added this
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.base-url:http://localhost:8082}")
    private String baseUrl;

    @PostConstruct
    @Transactional
    public void initSuperAdmin() {
        try {
            // Check if super admin exists with this account number before creating
            if (!repository.existsByAccountNumber("0000000000")) {
                // Only create if it doesn't exist
                RestaurantAdmin superAdmin = RestaurantAdmin.builder()
                        .email("ashanisamarakoon36@gmail.com")
                        .password(passwordEncoder.encode("ixfyqkskuswmhkok"))
                        .role(RestaurantAdmin.Role.valueOf("SUPER_ADMIN"))
                        .ownerName("Super Admin")
                        .restaurantName("System Admin")
                        .phone("0000000000")
                        .nic("0000000000V")
                        .address("System Address")
                        .accountNumber("0000000000")
                        .bankName("System Bank")
                        .branchName("System Branch")
                        .bankAccountOwner("System")
                        .isVerified(true)
                        .build();

                repository.save(superAdmin);
                log.info("Super admin account initialized successfully");
            } else {
                log.info("Super admin account already exists, skipping initialization");
            }
        } catch (Exception e) {
            log.error("Failed to initialize super admin account", e);
        }
    }


    @Transactional
    public String generateVerificationToken(RestaurantAdmin admin) {
        // First check if a token already exists for this user
        Optional<VerificationToken> existingToken = tokenRepository.findByRestaurantAdminEmail(admin.getEmail());

        // If exists, delete it as we'll create a new one
        existingToken.ifPresent(tokenRepository::delete);

        // Create new token
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .restaurantAdmin(admin)
                .expiryDate(LocalDateTime.now().plusHours(24)) // Token valid for 24 hours
                .build();

        tokenRepository.save(verificationToken);
        log.info("Generated verification token for: {}", admin.getEmail());

        return token;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Processing registration request for email: {}", request.getEmail());

        if (repository.existsByEmail(request.getEmail())) {
            log.warn("Registration attempt with existing email: {}", request.getEmail());
            throw new RuntimeException("Email already in use");
        }
        if (repository.existsByNic(request.getNic())) {
            log.warn("Registration attempt with existing NIC: {}", request.getNic());
            throw new RuntimeException("NIC already registered");
        }
        if (repository.existsByAccountNumber(request.getAccountNumber())) {
            log.warn("Registration attempt with existing account number: {}", request.getAccountNumber());
            throw new RuntimeException("Bank account already registered");
        }

        log.info("Creating new admin account for email: {}", request.getEmail());
        var admin = RestaurantAdmin.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .restaurantName(request.getRestaurantName())
                .ownerName(request.getOwnerName())
                .nic(request.getNic())
                .phone(request.getPhone())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .bankAccountOwner(request.getBankAccountOwner())
                .bankName(request.getBankName())
                .branchName(request.getBranchName())
                .accountNumber(request.getAccountNumber())
                .role(RestaurantAdmin.Role.RESTAURANT_ADMIN)
                .isVerified(false)
                .build();

        repository.save(admin);
        log.info("New admin registered successfully with ID: {}", admin.getId());

        // Send verification email
        try {
            log.info("Starting to prepare verification email for: {}", request.getEmail());

            // Generate a token and store it
            String token = generateVerificationToken(admin);

            String verificationLink = baseUrl + "/api/auth/verify?token=" + token;
            log.info("Generated verification link: {}", verificationLink);

            // Send the email
            emailService.sendVerificationEmail(
                    admin.getEmail(),
                    admin.getOwnerName(),
                    verificationLink
            );
            log.info("Verification email sent successfully to: {}", admin.getEmail());

            // Notify super admin
            emailService.notifySuperAdmin(
                    admin.getRestaurantName(),
                    admin.getOwnerName(),
                    admin.getEmail()
            );
            log.info("Super admin notification sent successfully");

        } catch (Exception e) {
            log.error("Failed to send verification emails for {}: {}", request.getEmail(), e.getMessage(), e);
            // We continue despite email sending failure - the admin can still verify the account manually
        }

        return RegisterResponse.builder()
                .email(admin.getEmail())
                .restaurantName(admin.getRestaurantName())
                .message("Registration successful! Please check your email for verification instructions or wait for admin verification.")
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
            log.info("Authentication attempt for: {}", request.getEmail());

            // First verify credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // If authentication passed, get the user
            RestaurantAdmin admin = repository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Check verification for restaurant admins only
            if (admin.getRole() == RestaurantAdmin.Role.RESTAURANT_ADMIN && !admin.isVerified()) {
                log.warn("Login attempt by unverified user: {}", admin.getEmail());
                throw new AccountNotVerifiedException(
                        "Your account has not been verified yet. Please check your email for verification instructions or wait for admin verification."
                );
            }

            log.info("Authenticated user: {} with role: {}", admin.getEmail(), admin.getRole());

            // Generate token for verified user
            String jwtToken = jwtService.generateToken(admin);

            return AuthResponse.builder()
                    .token(jwtToken)
                    .role(admin.getRole().name())
                    .email(admin.getEmail())
                    .restaurantName(admin.getRestaurantName())
                    .isVerified(admin.isVerified())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed: Invalid credentials for {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    public Optional<RestaurantAdmin> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Transactional
    public boolean verifyUser(String token) {
        log.info("Processing verification for token: {}", token);
        return tokenRepository.findByToken(token)
                .map(verificationToken -> {
                    if (verificationToken.isExpired()) {
                        log.warn("Attempted to use expired token for: {}",
                                verificationToken.getRestaurantAdmin().getEmail());
                        return false;
                    }

                    RestaurantAdmin admin = verificationToken.getRestaurantAdmin();
                    admin.setIsVerified(true);
                    repository.save(admin);

                    // Clean up the token
                    tokenRepository.delete(verificationToken);

                    log.info("Successfully verified user: {}", admin.getEmail());
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("Attempted to use invalid token: {}", token);
                    return false;
                });
    }
}