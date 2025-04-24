package com.example.restaurantauth.service;

import com.example.restaurantauth.config.JwtService;
import com.example.restaurantauth.dto.AuthRequest;
import com.example.restaurantauth.dto.AuthResponse;
import com.example.restaurantauth.dto.RegisterRequest;
import com.example.restaurantauth.model.RestaurantAdmin;
import com.example.restaurantauth.repository.RestaurantAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RestaurantAdminRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        var admin = RestaurantAdmin.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .restaurantName(request.getRestaurantName())
                .phone(request.getPhone())
                .isVerified(false) // Admin needs to verify first
                .build();

        repository.save(admin);

        var jwtToken = jwtService.generateToken(admin);
        return AuthResponse.builder()
                .token(jwtToken)
                .email(admin.getEmail())
                .restaurantName(admin.getRestaurantName())
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var admin = repository.findByEmail(request.getEmail())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(admin);
        return AuthResponse.builder()
                .token(jwtToken)
                .email(admin.getEmail())
                .restaurantName(admin.getRestaurantName())
                .build();
    }
}