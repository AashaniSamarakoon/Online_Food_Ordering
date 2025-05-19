package com.example.restaurantservice.repository;

import com.example.restaurantservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Added Repository annotation
public interface OrderRepository extends JpaRepository<Order, Long> {
    // No extra code needed for saving
}