package com.example.restaurantservice.repository;

import com.example.restaurantservice.model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    Page<MenuItem> findByRestaurantId(Long restaurantId, Pageable pageable);

    Page<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId, Pageable pageable);

    Page<MenuItem> findByRestaurantIdAndCategoryIgnoreCase(Long restaurantId, String category, Pageable pageable);

    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndCategoryIgnoreCase(Long restaurantId, String category);

    @Modifying
    @Query("UPDATE MenuItem m SET m.available = :available WHERE m.restaurant.id = :restaurantId")
    void updateAvailabilityByRestaurant(@Param("restaurantId") Long restaurantId,
                                        @Param("available") Boolean available);

    long countByRestaurantId(Long restaurantId);

    Optional<MenuItem> findByIdAndRestaurantId(Long menuItemId, Long restaurantId);
}