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
    Optional<Restaurant> findByOwnerId(String ownerId);
    boolean existsByOwnerId(String ownerId);
    boolean existsByIdAndOwnerId(Long id, String ownerId);
    Optional<Restaurant> findByIdAndOwnerId(Long id, String ownerId);
    List<Restaurant> findByIsActiveTrue();
    List<Restaurant> findByNameContainingIgnoreCase(String name);

    @Query("SELECT r FROM Restaurant r LEFT JOIN FETCH r.menuItems WHERE r.id = :id")
    Optional<Restaurant> findByIdWithMenuItems(@Param("id") Long id);
}