package com.example.restaurantservice.repository;

import com.example.restaurantservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderManagementRepository extends JpaRepository<Order, Long> {
    List<Order> findByRestaurantId(Long restaurantId);
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, String status);
    Optional<Order> findByOrderIdAndRestaurantId(Long orderId, Long restaurantId);
}