package com.example.restaurantservice.repository;

import com.example.restaurantservice.model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Paginated query
    Page<MenuItem> findByRestaurantId(Long restaurantId, Pageable pageable);

    // Non-paginated query
    List<MenuItem> findByRestaurantId(Long restaurantId);

    // Other queries
    Page<MenuItem> findByRestaurantIdAndStatus(Long restaurantId, MenuItem.ItemStatus status, Pageable pageable);
    Page<MenuItem> findByRestaurantIdAndCategoryIgnoreCase(Long restaurantId, String category, Pageable pageable);
    Page<MenuItem> findByRestaurantIdAndNameContainingIgnoreCase(Long restaurantId, String name, Pageable pageable);
    Optional<MenuItem> findByIdAndRestaurantId(Long menuItemId, Long restaurantId);
    boolean existsByIdAndRestaurantId(Long menuItemId, Long restaurantId);
}