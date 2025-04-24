package com.example.restaurantauth.repository;

import com.example.restaurantauth.model.RestaurantAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantAdminRepository extends JpaRepository<RestaurantAdmin, Long> {
    Optional<RestaurantAdmin> findByEmail(String email);
    boolean existsByEmail(String email);
}
