package com.example.restaurantservice.repository;

import com.example.restaurantservice.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Restaurant> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
            String name, String address, Pageable pageable);

    List<Restaurant> findByLatitudeBetweenAndLongitudeBetweenAndIsActiveTrue(
            Double minLat, Double maxLat, Double minLon, Double maxLon, Pageable pageable);

    // Added method for finding restaurants by isActive status with pagination
    Page<Restaurant> findByIsActiveTrue(Pageable pageable);

//    // Added method for finding top restaurants by rating or some other criteria
//    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true ORDER BY r.rating DESC")
//    List<Restaurant> findTopActiveRestaurants(Pageable pageable);

    // Added method to find a restaurant by username (if needed based on your service)
    Optional<Restaurant> findByUsername(String username);

    // Added method to find by ownerUsername if that's what your model uses
    Optional<Restaurant> findByOwnerUsername(String ownerUsername);
}