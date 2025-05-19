package com.example.restaurantservice.repository;

import com.example.restaurantservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // No extra code needed for saving
}