package com.delivery.driverauthservice.security;

import com.delivery.driverauthservice.model.DriverCredential;
import com.delivery.driverauthservice.repository.DriverCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final DriverCredentialRepository driverCredentialRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        DriverCredential driver = driverCredentialRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        CustomUserDetails userDetails = new CustomUserDetails(
                driver.getUsername(),
                driver.getPassword(),
                driver.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );

        userDetails.setDriverId(driver.getDriverId());
        userDetails.setAccountLocked(driver.isAccountLocked());
        userDetails.setPhoneVerified(driver.isPhoneVerified());

        return userDetails;
    }
}