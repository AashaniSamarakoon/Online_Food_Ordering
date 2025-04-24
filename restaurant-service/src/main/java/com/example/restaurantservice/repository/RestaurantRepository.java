package com.example.restaurantservice.repository;

import com.example.restaurantservice.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Find restaurant by admin ID (from auth service)
    Optional<Restaurant> findByAdminId(String adminId);

    // Find all active restaurants
    List<Restaurant> findByIsActiveTrue();

    // Find restaurants by name (case-insensitive search)
    List<Restaurant> findByNameContainingIgnoreCase(String name);

    // Custom query with join fetch for menu items
    @Query("SELECT r FROM Restaurant r LEFT JOIN FETCH r.menuItems WHERE r.id = :id")
    Optional<Restaurant> findByIdWithMenuItems(@Param("id") Long id);

    // Check if restaurant exists by email
    boolean existsByEmail(String email);
}