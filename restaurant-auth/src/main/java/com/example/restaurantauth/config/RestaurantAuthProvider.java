//package com.example.restaurantauth.config;
//
//import com.example.restaurantauth.service.RestaurantAdminService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class RestaurantAuthProvider implements AuthenticationProvider {
//    private final RestaurantAdminService adminService;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        String username = authentication.getName();
//        String password = authentication.getCredentials().toString();
//
//        UserDetails admin = adminService.loadUserByUsername(username);
//
//        if (passwordEncoder.matches(password, admin.getPassword())) {
//            return new UsernamePasswordAuthenticationToken(
//                    admin,
//                    password,
//                    admin.getAuthorities()
//            );
//        }
//        throw new BadCredentialsException("Invalid credentials");
//    }
//
//    @Override
//    public boolean supports(Class<?> authentication) {
//        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
//    }
//}

package com.example.restaurantauth.config;

import com.example.restaurantauth.exception.AccountNotVerifiedException;
import com.example.restaurantauth.model.RestaurantAdmin;
import com.example.restaurantauth.repository.RestaurantAdminRepository;
import com.example.restaurantauth.service.RestaurantAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class RestaurantAuthProvider implements AuthenticationProvider {
    private final RestaurantAdminService adminService;
    private final RestaurantAdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // First check if the user exists
        RestaurantAdmin admin = adminRepository.findByEmail(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check if password matches
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Check if the account is verified (skip for super admins)
        if (admin.getRole() != RestaurantAdmin.Role.SUPER_ADMIN && !admin.isVerified()) {
            throw new AccountNotVerifiedException(
                    "Your account has not been verified yet. Please wait for admin verification."
            );
        }

        // Authentication successful - create authenticated token with authorities
        return new UsernamePasswordAuthenticationToken(
                admin, // Principal - the full admin object
                password, // Credentials
                Collections.singletonList(new SimpleGrantedAuthority(admin.getRole().name())) // Authorities
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}