package com.example.restaurantauth.service;

import com.example.restaurantauth.model.RestaurantAdmin;
import com.example.restaurantauth.repository.RestaurantAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantAdminService implements UserDetailsService {
    private final RestaurantAdminRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));


    }
}
