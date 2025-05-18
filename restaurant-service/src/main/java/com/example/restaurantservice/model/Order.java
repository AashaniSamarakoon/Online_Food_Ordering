package com.example.restaurantservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;  // Changed to match "orderId" in image

    @Column(name = "restaurant_name", nullable = false)
    private String restaurantName;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    private String status = "PLACED";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}