package com.example.restaurantservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Long restaurantId;
    private LocalDate date;
    private String description;
    private String bankName;
    private Double amount;

    // Add JSON property annotations if the API response uses different field names
    // Example:
    // @JsonProperty("transaction_id")
    // private Long id;
}