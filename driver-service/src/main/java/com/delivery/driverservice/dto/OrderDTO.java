package com.delivery.driverservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private Long driverId;
    private String status; // CREATED, ASSIGNED, DELIVERED, etc.
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private Long menuItemId;
        private String name;
        private Integer quantity;
        private Double price;
    }
}