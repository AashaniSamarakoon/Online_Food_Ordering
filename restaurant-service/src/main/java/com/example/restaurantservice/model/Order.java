//package com.example.restaurantservice.model;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Entity
//@Table(name = "orders")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Order {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long orderId;  // Note: Using orderId to match the field in restaurant-service
//
//    private Long userId;
//
//    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(name = "order_items",
//            joinColumns = @JoinColumn(name = "order_id"))
//    private List<OrderedItem> items;
//
//    private Long restaurantId;
//    private String restaurantName;
//
//    private String address;
//    private String restaurantAddress;
//    private String username;
//    private String email;
//    private String phoneNumber;
//
//    private Double totalPrice = 0.0;
//
//    private Double deliveryCharges;
//
//    private String status;
//
//    private String deliveryInstructions;
//
//    private String paymentMethod;
//    private String paymentStatus;
//    private String transactionId;
//
//    private Long assignedDeliveryPersonId;
//    private String cancellationReason;
//
//    private LocalDateTime orderTime;
//    private LocalDateTime deliveryTimeEstimate;
//
//    @Embedded
//    @AttributeOverrides({
//            @AttributeOverride(name = "latitude", column = @Column(name = "restaurant_latitude")),
//            @AttributeOverride(name = "longitude", column = @Column(name = "restaurant_longitude"))
//    })
//    private Coordinates restaurantCoordinates;
//
//    @Embedded
//    @AttributeOverrides({
//            @AttributeOverride(name = "latitude", column = @Column(name = "customer_latitude")),
//            @AttributeOverride(name = "longitude", column = @Column(name = "customer_longitude"))
//    })
//    private Coordinates customerCoordinates;
//
//    @CreationTimestamp
//    @Column(updatable = false)
//    private LocalDateTime createdAt;
//
//    // Mapping for compatibility between order service's 'id' and restaurant service's 'orderId'
//    public Long getId() {
//        return orderId;
//    }
//
//    public void setId(Long id) {
//        this.orderId = id;
//    }
//}

package com.example.restaurantservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private Long orderId;

    // Added field to reference the order ID from order-service
    private Long externalOrderId;

    private Long userId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items",
            joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderedItem> items;

    private Long restaurantId;
    private String restaurantName;

    private String address;
    private String restaurantAddress;
    private String username;
    private String email;
    private String phoneNumber;

    private Double totalPrice = 0.0;

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