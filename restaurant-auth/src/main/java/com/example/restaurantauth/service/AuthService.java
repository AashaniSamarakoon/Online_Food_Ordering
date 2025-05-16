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
//            if (!repository.existsByEmail("superadmin@example.com")) {
//                RestaurantAdmin superAdmin = RestaurantAdmin.builder()
//                        .email("superadmin@example.com")
//                        .password(passwordEncoder.encode("superadmin123"))
//                        .restaurantName("System Administration")
//                        .ownerName("System Admin")
//                        .nic("000000000V")
//                        .phone("0000000000")
//                        .address("System Address")
//                        .latitude(0.0)
//                        .longitude(0.0)
//                        .bankAccountOwner("System Admin")
//                        .bankName("System Bank")
//                        .branchName("System Branch")
//                        .accountNumber("0000000000")
//                        .role(RestaurantAdmin.Role.SUPER_ADMIN)
//                        .isVerified(true)
//                        .build();
//                repository.save(superAdmin);
//                log.info("Super admin account initialized successfully");
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
//    // In your AuthService.java
//    public AuthResponse authenticate(AuthRequest request) {
//        RestaurantAdmin admin = repository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        // Debug: Log actual role from database
//        log.info("Authenticating {} with role: {}", admin.getEmail(), admin.getRole());
//
//        // Force new token generation with current DB state
//        String jwtToken = jwtService.generateToken(admin);
//
//        return AuthResponse.builder()
//                .token(jwtToken)
//                .role(admin.getRole().name()) // Get directly from DB
//                .build();
//    }
//}

package com.example.restaurantauth.service;

import com.example.restaurantauth.config.JwtService;
import com.example.restaurantauth.dto.*;
import com.example.restaurantauth.exception.AccountNotVerifiedException;
import com.example.restaurantauth.model.RestaurantAdmin;
import com.example.restaurantauth.repository.RestaurantAdminRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final RestaurantAdminRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostConstruct
    @Transactional
    public void initSuperAdmin() {
        try {
            if (!repository.existsByEmail("superadmin@example.com")) {
                RestaurantAdmin superAdmin = RestaurantAdmin.builder()
                        .email("superadmin@example.com")
                        .password(passwordEncoder.encode("superadmin123"))
                        .restaurantName("System Administration")
                        .ownerName("System Admin")
                        .nic("000000000V")
                        .phone("0000000000")
                        .address("System Address")
                        .latitude(0.0)
                        .longitude(0.0)
                        .bankAccountOwner("System Admin")
                        .bankName("System Bank")
                        .branchName("System Branch")
                        .accountNumber("0000000000")
                        .role(RestaurantAdmin.Role.SUPER_ADMIN)
                        .isVerified(true)
                        .build();
                repository.save(superAdmin);
                log.info("Super admin account initialized successfully");
            }
        } catch (Exception e) {
            log.error("Failed to initialize super admin account", e);
        }
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (repository.existsByNic(request.getNic())) {
            throw new RuntimeException("NIC already registered");
        }
        if (repository.existsByAccountNumber(request.getAccountNumber())) {
            throw new RuntimeException("Bank account already registered");
        }

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
        log.info("New admin registered: {}", request.getEmail());

        return RegisterResponse.builder()
                .email(admin.getEmail())
                .restaurantName(admin.getRestaurantName())
                .message("Registration successful! Please wait for verification.")
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
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
                        "Your account has not been verified yet. Please wait for admin verification."
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
}