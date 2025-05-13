package com.example.restaurantauth.service;

import com.example.restaurantauth.model.RestaurantAdmin;
import com.example.restaurantauth.repository.RestaurantAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantAdminService implements UserDetailsService {
    private final RestaurantAdminRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        RestaurantAdmin admin = repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                admin.getEmail(),
                admin.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name()))
        );
    }

    public Optional<RestaurantAdmin> findByEmail(String email) {
        return repository.findByEmail(email);
    }
}