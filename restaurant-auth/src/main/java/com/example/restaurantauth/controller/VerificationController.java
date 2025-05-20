package com.example.restaurantauth.controller;

import com.example.restaurantauth.model.RestaurantAdmin;
import com.example.restaurantauth.model.VerificationToken;
import com.example.restaurantauth.repository.RestaurantAdminRepository;
import com.example.restaurantauth.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class VerificationController {

    private final RestaurantAdminRepository adminRepository;
    private final VerificationTokenRepository tokenRepository;

    @GetMapping("/verify")
    @Transactional
    public ModelAndView verifyAccount(@RequestParam String token) {
        log.info("Received email verification request with token: {}", token);

        ModelAndView modelAndView = new ModelAndView("verification-result");

        try {
            VerificationToken verificationToken = tokenRepository.findByToken(token)
                    .orElse(null);

            if (verificationToken == null) {
                log.warn("Invalid verification token: {}", token);
                modelAndView.addObject("success", false);
                modelAndView.addObject("message", "Invalid verification link. Please contact support.");
                return modelAndView;
            }

            if (verificationToken.isExpired()) {
                log.warn("Token expired for user: {}", verificationToken.getRestaurantAdmin().getEmail());
                modelAndView.addObject("success", false);
                modelAndView.addObject("message", "Your verification link has expired. Please contact support to request a new one.");
                return modelAndView;
            }

            RestaurantAdmin admin = verificationToken.getRestaurantAdmin();
            admin.setIsVerified(true);
            adminRepository.save(admin);

            // Delete the used token
            tokenRepository.delete(verificationToken);

            log.info("User verified successfully: {}", admin.getEmail());
            modelAndView.addObject("success", true);
            modelAndView.addObject("message", "Your account has been verified successfully! You can now log in.");
            return modelAndView;

        } catch (Exception e) {
            log.error("Error verifying account: {}", e.getMessage(), e);
            modelAndView.addObject("success", false);
            modelAndView.addObject("message", "An error occurred during verification. Please try again or contact support.");
            return modelAndView;
        }
    }

    @GetMapping("/admin-verify")
    @Transactional
    public ModelAndView adminVerifyAccount(@RequestParam String email) {
        log.info("Admin verification link clicked for email: {}", email);

        ModelAndView modelAndView = new ModelAndView("verification-result");

        try {
            RestaurantAdmin adminToVerify = adminRepository.findByEmail(email)
                    .orElse(null);

            if (adminToVerify == null) {
                log.warn("No user found with email: {}", email);
                modelAndView.addObject("success", false);
                modelAndView.addObject("message", "User not found with the specified email address.");
                return modelAndView;
            }

            if (adminToVerify.isVerified()) {
                log.warn("Account {} is already verified", email);
                modelAndView.addObject("success", true);
                modelAndView.addObject("message", "This account has already been verified.");
                return modelAndView;
            }

            adminToVerify.setIsVerified(true);
            adminRepository.save(adminToVerify);

            log.info("Account {} successfully verified through admin email link", email);
            modelAndView.addObject("success", true);
            modelAndView.addObject("message", "Restaurant account has been verified successfully!");
            return modelAndView;

        } catch (Exception e) {
            log.error("Error during admin verification: {}", e.getMessage(), e);
            modelAndView.addObject("success", false);
            modelAndView.addObject("message", "An error occurred during verification. Please try again or verify through the admin panel.");
            return modelAndView;
        }
    }

    // For testing - can be removed in production
    @GetMapping("/test-verify")
    @Transactional
    public ResponseEntity<String> testVerify(@RequestParam String email) {
        log.info("Received test verification request for email: {}", email);

        try {
            RestaurantAdmin admin = adminRepository.findByEmail(email)
                    .orElse(null);

            if (admin == null) {
                return ResponseEntity.badRequest().body("User not found: " + email);
            }

            admin.setIsVerified(true);
            adminRepository.save(admin);
            log.info("User test-verified successfully: {}", email);
            return ResponseEntity.ok("Account verified successfully for testing");
        } catch (Exception e) {
            log.error("Error in test verification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}