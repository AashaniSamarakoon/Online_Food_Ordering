package com.example.restaurantservice.dto;

import com.example.restaurantservice.model.OrderedItem;
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
public class OrderListResponseDTO {
    private Long id;
    private String status;
    private String customer;
    private String contact;
    private Double total;
    private LocalDateTime createdAt;
    private String deliveryType;
    private LocalDateTime pickupTime;
    private int itemCount;
}