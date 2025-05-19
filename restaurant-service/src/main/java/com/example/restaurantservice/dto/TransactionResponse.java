package com.example.restaurantservice.dto;

import lombok.Data;

@Data
public class TransactionResponse {
    private Long id;
    private Long restaurantId;
    private String date;
    private String description;
    private String bankName;
    private Double amount;
}