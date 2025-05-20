package com.example.restaurantservice.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private String bankName;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "payment_service_transaction_id", nullable = false, unique = true)
    private Long paymentServiceTransactionId;
}