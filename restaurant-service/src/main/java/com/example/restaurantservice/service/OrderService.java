//package com.example.restaurantservice.service;
//
//import com.example.restaurantservice.model.Order;
//import com.example.restaurantservice.model.OrderedItem;
//import com.example.restaurantservice.repository.OrderRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class OrderService {
//
//    private final OrderRepository orderRepository;
//
//    @Transactional
//    public Order saveExternalOrder(Order externalOrder) {
//        // Validate essential fields
//        if (externalOrder == null) {
//            throw new IllegalArgumentException("Order cannot be null");
//        }
//
//        if (externalOrder.getRestaurantName() == null || externalOrder.getRestaurantName().isEmpty()) {
//            throw new IllegalArgumentException("Restaurant name is required");
//        }
//
//        if (externalOrder.getRestaurantId() == null) {
//            throw new IllegalArgumentException("Restaurant ID is required");
//        }
//
//        if (externalOrder.getItems() == null || externalOrder.getItems().isEmpty()) {
//            throw new IllegalArgumentException("Order must contain at least one item");
//        }
//
//        // Set default values for fields that might be null
//        if (externalOrder.getStatus() == null) {
//            externalOrder.setStatus("PLACED");
//        }
//
//        if (externalOrder.getOrderTime() == null) {
//            externalOrder.setOrderTime(LocalDateTime.now());
//        }
//
//        // Calculate total price if not provided
//        if (externalOrder.getTotalPrice() == null || externalOrder.getTotalPrice() == 0.0) {
//            double total = 0.0;
//            for (OrderedItem item : externalOrder.getItems()) {
//                if (item.getSubtotal() != null) {
//                    total += item.getSubtotal();
//                } else if (item.getPrice() != null && item.getQuantity() != null) {
//                    total += item.getPrice() * item.getQuantity();
//                }
//            }
//            externalOrder.setTotalPrice(total);
//        }
//
//        log.info("Saving order from restaurant: {}, with {} items",
//                externalOrder.getRestaurantName(),
//                externalOrder.getItems().size());
//
//        return orderRepository.save(externalOrder);
//    }
//}

package com.example.restaurantservice.service;

import com.example.restaurantservice.dto.OrderDTO;
import com.example.restaurantservice.mapper.OrderMapper;
import com.example.restaurantservice.model.Order;
import com.example.restaurantservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Order saveExternalOrder(OrderDTO orderDTO) {
        // Validate essential fields
        if (orderDTO == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        if (orderDTO.getRestaurantName() == null || orderDTO.getRestaurantName().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name is required");
        }

        if (orderDTO.getRestaurantId() == null) {
            throw new IllegalArgumentException("Restaurant ID is required");
        }

        if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Convert DTO to entity
        Order order = OrderMapper.toEntity(orderDTO);

        // Ensure proper timestamps
        if (order.getOrderTime() == null) {
            order.setOrderTime(LocalDateTime.now());
        }

        log.info("Saving order from restaurant: {}, with {} items, external order ID: {}",
                order.getRestaurantName(),
                order.getItems().size(),
                order.getExternalOrderId());

        return orderRepository.save(order);
    }
}