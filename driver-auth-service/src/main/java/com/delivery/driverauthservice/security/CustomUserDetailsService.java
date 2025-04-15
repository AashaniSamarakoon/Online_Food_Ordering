package com.delivery.driverauthservice.security;

import com.delivery.driverauthservice.model.DriverCredential;
import com.delivery.driverauthservice.repository.DriverCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final DriverCredentialRepository driverCredentialRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // Try to find by username, email, or phone number
        Optional<DriverCredential> driverOpt = driverCredentialRepository.findByUsername(login);

        if (driverOpt.isEmpty()) {
            driverOpt = driverCredentialRepository.findByEmail(login);
        }

        if (driverOpt.isEmpty()) {
            driverOpt = driverCredentialRepository.findByPhoneNumber(login);
        }

        DriverCredential driver = driverOpt.orElseThrow(() ->
                new UsernameNotFoundException("User not found with username/email/phone: " + login));

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