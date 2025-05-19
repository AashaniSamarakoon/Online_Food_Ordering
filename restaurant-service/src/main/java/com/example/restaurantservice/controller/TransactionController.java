package com.example.restaurantservice.controller;

import com.example.restaurantservice.model.Transaction;
import com.example.restaurantservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // API to sync (fetch and store) all transactions from payment-service
    @PostMapping("/sync")
    public ResponseEntity<List<Transaction>> syncTransactions() {
        List<Transaction> synced = transactionService.syncTransactionsFromPaymentService();
        return ResponseEntity.ok(synced);
    }

    // Get all locally stored transactions
    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }
}