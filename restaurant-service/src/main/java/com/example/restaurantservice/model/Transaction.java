package com.example.restaurantservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    private Long id;

    private Long restaurantId;

    private LocalDate date;

    private String description;

    private String bankName;

    private Double amount;
}