package com.order_service.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private List<OrderItemDTO> items;
    private Double totalPrice;
    private Double deliveryCharges;
    private String status;
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private CoordinatesDTO restaurantCoordinates;
    private CoordinatesDTO customerCoordinates;
    private LocalDateTime createdAt;
}