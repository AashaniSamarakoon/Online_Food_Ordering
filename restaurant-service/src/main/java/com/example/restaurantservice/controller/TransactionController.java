package com.example.restaurantservice.controller;

import com.example.restaurantservice.model.Transaction;
import com.example.restaurantservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // Only restaurant owner can sync their transactions
    @PreAuthorize("@restaurantAuthorizationService.isRestaurantOwner(authentication, #restaurantId)")
    @PostMapping("/sync/{restaurantId}")
    public ResponseEntity<List<Transaction>> syncTransactions(@PathVariable Long restaurantId) {
        List<Transaction> synced = transactionService.syncTransactionsFromPaymentService(restaurantId);
        return ResponseEntity.ok(synced);
    }

    // Only restaurant owner can view their transactions
    @PreAuthorize("@restaurantAuthorizationService.isRestaurantOwner(authentication, #restaurantId)")
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(transactionService.getTransactionsByRestaurant(restaurantId));
    }
}