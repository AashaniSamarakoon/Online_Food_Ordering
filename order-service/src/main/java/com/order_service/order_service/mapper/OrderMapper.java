package com.order_service.order_service.mapper;

import com.order_service.order_service.dto.CoordinatesDTO;
import com.order_service.order_service.dto.OrderDTO;
import com.order_service.order_service.dto.OrderItemDTO;
import com.order_service.order_service.model.Coordinates;
import com.order_service.order_service.model.Order;
import com.order_service.order_service.model.OrderedItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDTO toDTO(Order order) {
        if (order == null) return null;

        return OrderDTO.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .restaurantId(order.getRestaurantId())
                .restaurantName(order.getRestaurantName())
                .restaurantAddress(order.getRestaurantAddress())
                .items(mapOrderItems(order.getItems()))
                .totalPrice(order.getTotalPrice())
                .deliveryCharges(order.getDeliveryCharges())
                .status(order.getStatus())
                .username(order.getUsername())
                .email(order.getEmail())
                .phoneNumber(order.getPhoneNumber())
                .address(order.getAddress())
                .restaurantCoordinates(mapCoordinates(order.getRestaurantCoordinates()))
                .customerCoordinates(mapCoordinates(order.getCustomerCoordinates()))
                .createdAt(order.getCreatedAt())
                .build();
    }

    private static List<OrderItemDTO> mapOrderItems(List<OrderedItem> items) {
        if (items == null) return null;
        return items.stream()
                .map(item -> OrderItemDTO.builder()
                        .foodItemId(item.getFoodItemId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .category(item.getCategory())
                        .imageUrl(item.getImageUrl())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());
    }

    private static CoordinatesDTO mapCoordinates(Coordinates coordinates) {
        if (coordinates == null) return null;
        return new CoordinatesDTO(coordinates.getLatitude(), coordinates.getLongitude());
    }
}