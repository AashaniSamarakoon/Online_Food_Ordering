package com.example.restaurantservice.service;

import com.example.restaurantservice.dto.*;
import com.example.restaurantservice.model.Order;
import com.example.restaurantservice.model.OrderedItem;
import com.example.restaurantservice.repository.OrderManagementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderManagementService {  // Renamed from OrderServices

    private final OrderManagementRepository orderManagementRepository;

    /**
     * Get all orders for a specific restaurant
     */
    public List<OrderListResponseDTO> getOrdersByRestaurant(Long restaurantId) {
        List<Order> orders = orderManagementRepository.findByRestaurantId(restaurantId);
        return orders.stream()
                .map(this::mapToOrderListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by restaurant and status
     */
    public List<OrderListResponseDTO> getOrdersByRestaurantAndStatus(Long restaurantId, String status) {
        List<Order> orders = orderManagementRepository.findByRestaurantIdAndStatus(restaurantId, status.toUpperCase());
        return orders.stream()
                .map(this::mapToOrderListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get order details by ID
     */
    public OrderDetailResponseDTO getOrderById(Long orderId, Long restaurantId) {
        return orderManagementRepository.findByOrderIdAndRestaurantId(orderId, restaurantId)
                .map(this::mapToOrderDetailDTO)
                .orElse(null);
    }

    /**
     * Update order status
     */
    @Transactional
    public OrderDetailResponseDTO updateOrderStatus(Long orderId, String status, Long restaurantId) {
        Order order = orderManagementRepository.findByOrderIdAndRestaurantId(orderId, restaurantId)
                .orElseThrow(() -> new RuntimeException("Order not found or doesn't belong to this restaurant"));

        order.setStatus(status.toUpperCase());
        Order updatedOrder = orderManagementRepository.save(order);

        return mapToOrderDetailDTO(updatedOrder);
    }

    /**
     * Get order status
     */
    public OrderStatusDTO getOrderStatus(Long orderId, Long restaurantId) {
        return orderManagementRepository.findByOrderIdAndRestaurantId(orderId, restaurantId)
                .map(order -> new OrderStatusDTO(order.getStatus(), getStatusMessage(order.getStatus())))
                .orElse(null);
    }

    /**
     * Map Order entity to OrderListResponseDTO
     */
    private OrderListResponseDTO mapToOrderListDTO(Order order) {
        return OrderListResponseDTO.builder()
                .id(order.getOrderId())
                .status(order.getStatus())
                .customer(order.getUsername())
                .contact(order.getPhoneNumber())
                .total(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .deliveryType(determineDeliveryType(order))
                .pickupTime(order.getOrderTime())
                .itemCount(order.getItems() != null ? order.getItems().size() : 0)
                .build();
    }

    /**
     * Map Order entity to OrderDetailResponseDTO
     */
    private OrderDetailResponseDTO mapToOrderDetailDTO(Order order) {
        // Map items
        List<OrderedItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> new OrderedItemDTO(
                        item.getName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());

        // For this example we'll assume no driver assignment functionality yet
        DriverDTO driverDTO = null;
        // In a real app, you might check if a driver is assigned and create the DTO

        return OrderDetailResponseDTO.builder()
                .id(order.getOrderId())
                .status(order.getStatus())
                .customer(order.getUsername())
                .contact(order.getPhoneNumber())
                .total(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .items(itemDTOs)
                .deliveryType(determineDeliveryType(order))
                .pickupTime(order.getOrderTime())
                .driver(driverDTO)
                .eta(calculateETA(order))
                .build();
    }

    /**
     * Determine if this is delivery or pickup
     */
    private String determineDeliveryType(Order order) {
        // You can determine this based on your domain logic
        // For example, if customer coordinates are present, it's likely delivery
        return order.getCustomerCoordinates() != null ? "delivery" : "pickup";
    }

    /**
     * Calculate estimated time of arrival or completion
     */
    private String calculateETA(Order order) {
        // This is a placeholder - in a real app you'd have logic to calculate this
        // based on order status, driver location, etc.
        return "15-20 minutes";
    }

    /**
     * Get a user-friendly message for each status
     */
    private String getStatusMessage(String status) {
        switch (status) {
            case "PLACED":
                return "Order has been received";
            case "IN_PROGRESS":
                return "Order is being processed";
            case "PREPARING":
                return "Food is being prepared";
            case "READY_FOR_PICKUP":
                return "Order is ready for pickup";
            case "ON_THE_WAY":
                return "Driver is on the way";
            case "DELIVERED":
                return "Order has been delivered";
            case "COMPLETED":
                return "Order has been completed";
            default:
                return "Status unknown";
        }
    }
}