package com.example.restaurantauth.controller;

import com.example.restaurantauth.exception.UserNotFoundException;
import com.example.restaurantauth.model.RestaurantAdmin;
import com.example.restaurantauth.repository.RestaurantAdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final RestaurantAdminRepository adminRepository;

    @PostMapping("/verify-account")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> verifyAccount(
            @RequestParam String email,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Account verification request received for email: {}", email);
        log.debug("Request initiated by admin: {} with roles: {}",
                userDetails.getUsername(), userDetails.getAuthorities());

        RestaurantAdmin adminToVerify = adminRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });

        if (adminToVerify.isVerified()) {
            log.warn("Account {} is already verified", email);
            return ResponseEntity.badRequest().body("Account is already verified");
        }

        adminToVerify.setVerified(true);
        adminRepository.save(adminToVerify);

        log.info("Account {} successfully verified by admin {}",
                email, userDetails.getUsername());

        return ResponseEntity.ok("Account " + email + " verified successfully");
    }
}