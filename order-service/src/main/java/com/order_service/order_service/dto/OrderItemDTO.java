package com.order_service.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long foodItemId;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private Double price;
    private Integer quantity;
    private Double subtotal;
}