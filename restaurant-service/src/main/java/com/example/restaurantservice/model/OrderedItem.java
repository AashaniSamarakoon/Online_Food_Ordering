package com.example.restaurantservice.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderedItem {

    private Long foodItemId;

    private String name;
    private String description;
    private String category;
    private String imageUrl;

    private Double price;
    private Integer quantity;
    private Double subtotal;

    private boolean available;

    private String customizations;
    private String notes;

    private Double tax;
    private Double discount;
}