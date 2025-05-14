package com.order_service.order_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ElementCollection
    private List<OrderedItem> items;

    private Long restaurantId;

    private Double totalPrice= 0.0;

    private Double deliveryCharges;

    private String status;

    private String deliveryInstructions;

    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;

    private Long assignedDeliveryPersonId;
    private String cancellationReason;

    private LocalDateTime orderTime;
    private LocalDateTime deliveryTimeEstimate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "restaurant_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "restaurant_longitude"))
    })
    private Coordinates restaurantCoordinates;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "customer_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "customer_longitude"))
    })
    private Coordinates customerCoordinates;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
