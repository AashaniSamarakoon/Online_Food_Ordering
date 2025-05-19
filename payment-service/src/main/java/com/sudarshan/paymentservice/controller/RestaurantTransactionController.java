package com.sudarshan.paymentservice.controller;

import com.sudarshan.paymentservice.entity.RestaurantTransaction;
import com.sudarshan.paymentservice.service.RestaurantTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/transactions")
@RequiredArgsConstructor
public class RestaurantTransactionController {

    private final RestaurantTransactionService transactionService;

    // GET all transactions for a restaurant
    @GetMapping("/restaurant/{restaurantId}")
    public List<RestaurantTransaction> getTransactionsByRestaurantId(
            @PathVariable Long restaurantId) {
        return transactionService.getTransactionsByRestaurantId(restaurantId);
    }

    // GET transactions for a restaurant on a specific date
    @GetMapping("/restaurant/{restaurantId}/date")
    public List<RestaurantTransaction> getTransactionsByRestaurantIdAndDate(
            @PathVariable Long restaurantId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return transactionService.getTransactionsByRestaurantIdAndDate(restaurantId, date);
    }
}
