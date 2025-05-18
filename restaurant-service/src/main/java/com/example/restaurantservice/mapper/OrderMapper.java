package com.example.restaurantservice.mapper;

import com.example.restaurantservice.dto.CoordinatesDTO;
import com.example.restaurantservice.dto.OrderDTO;
import com.example.restaurantservice.dto.OrderItemDTO;
import com.example.restaurantservice.model.Coordinates;
import com.example.restaurantservice.model.Order;
import com.example.restaurantservice.model.OrderedItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static Order toEntity(OrderDTO dto) {
        if (dto == null) return null;

        Order order = Order.builder()
                .userId(dto.getUserId())
                .restaurantId(dto.getRestaurantId())
                .restaurantName(dto.getRestaurantName())
                .restaurantAddress(dto.getRestaurantAddress())
                .items(mapOrderItems(dto.getItems()))
                .totalPrice(dto.getTotalPrice())
                .deliveryCharges(dto.getDeliveryCharges())
                .status(dto.getStatus())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .restaurantCoordinates(mapCoordinates(dto.getRestaurantCoordinates()))
                .customerCoordinates(mapCoordinates(dto.getCustomerCoordinates()))
                .build();

        // Store the external order ID for reference
        order.setExternalOrderId(dto.getOrderId());

        return order;
    }

    private static List<OrderedItem> mapOrderItems(List<OrderItemDTO> items) {
        if (items == null) return null;
        return items.stream()
                .map(item -> OrderedItem.builder()
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

    private static Coordinates mapCoordinates(CoordinatesDTO coordinates) {
        if (coordinates == null) return null;
        return Coordinates.builder()
                .latitude(coordinates.getLatitude())
                .longitude(coordinates.getLongitude())
                .build();
    }
}