package com.order_service.order_service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // associated user

    private String status; // e.g., "ACTIVE", "COMPLETED", "ABANDONED"

    private Long restaurantId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "total_price")
    private Double totalPrice; // total price including discounts, delivery, etc.

    @Column(name = "discount_code")
    private String discountCode; // store discount code applied

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // cart creation timestamp

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // cart last update timestamp


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
