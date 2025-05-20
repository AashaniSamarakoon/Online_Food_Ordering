package com.example.restaurantservice.repository;

import com.example.restaurantservice.model.AssignedDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignedDriverRepository extends JpaRepository<AssignedDriver, Long> {
    Optional<AssignedDriver> findByOrderId(Long orderId);
    List<AssignedDriver> findByRestaurantId(Long restaurantId);
    List<AssignedDriver> findByDriverId(Long driverId);
    List<AssignedDriver> findByRestaurantIdAndIsDelivered(Long restaurantId, boolean isDelivered);
}
