package com.example.restaurantservice.dto;

import lombok.Data;

@Data
public class TransactionResponse {
    private Long id;
    private Long restaurantId;
    private String date;        // Will parse to LocalDate in service
    private String description;
    private String bankName;
    private Long amount;
}