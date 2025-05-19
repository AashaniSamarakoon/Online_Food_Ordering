package com.delivery.orderassignmentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDetailsDTO {
    private Long id;

    // User information
    private String username;
    private String address;
    private String phoneNumber;

    // Restaurant information
    private String restaurantName;
    private String restaurantAddress;

    // Coordinates
    private LocationDTO restaurantCoordinates;
    private LocationDTO customerCoordinates;

    // Order details
    private List<OrderItemDTO> items;
    private Double totalPrice;
    private Double deliveryCharges;

    // These can be set after retrieval if needed by your business logic
    private LocalDateTime createdAt;
    private LocalDateTime assignmentTime;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderItemDTO {
        private Long foodItemId;
        private String name;
        private String description;
        private String category;
        private String imageUrl;
        private Double price;
        private Integer quantity;
        private Double subtotal;
        private Boolean available;
        private Object customizations;
        private String notes;
        private Double tax;
        private Double discount;
    }
}