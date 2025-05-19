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
public class OrderDetailResponseDTO {
    private Long id;
    private String status;
    private String customer;
    private String contact;
    private Double total;
    private LocalDateTime createdAt;
    private List<OrderedItemDTO> items;
    private String deliveryType;
    private LocalDateTime pickupTime;
    private DriverDTO driver;
    private String eta;
}