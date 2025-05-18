package com.example.restaurantservice.service;

import com.example.restaurantservice.model.Order;
import com.example.restaurantservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order saveExternalOrder(Order order) {
        // Validate essential fields
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        if (order.getRestaurantName() == null || order.getRestaurantName().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name is required");
        }

        if (order.getRestaurantId() == null) {
            throw new IllegalArgumentException("Restaurant ID is required");
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Set default status if not provided
        if (order.getStatus() == null) {
            order.setStatus("PLACED");
        }

        return orderRepository.save(order);
    }
}