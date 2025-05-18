package com.example.restaurantservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "food_item_id", nullable = false)
    private Long foodItemId;

    @Column(nullable = false)
    private String name;

    private String description;

    private String category;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal subtotal;

    private Boolean available;

    private String customizations;

    private String note;

    private String size;

    private BigDecimal discount;
}