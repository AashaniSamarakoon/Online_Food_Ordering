//package com.example.restaurantservice.controller;
//
//import com.example.restaurantservice.model.Transaction;
//import com.example.restaurantservice.service.TransactionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/restaurant/transactions/")
//@RequiredArgsConstructor
//public class TransactionController {
//
//    private final TransactionService transactionService;
//
//    // Only restaurant owner can sync their transactions
//    @PreAuthorize("@restaurantAuthorizationService.isRestaurantOwner(authentication, #restaurantId)")
//    @PostMapping("/sync/{restaurantId}")
//    public ResponseEntity<List<Transaction>> syncTransactions(@PathVariable Long restaurantId) {
//        List<Transaction> synced = transactionService.syncTransactionsFromPaymentService(restaurantId);
//        return ResponseEntity.ok(synced);
//    }
//
//    // Only restaurant owner can view their transactions
//    @PreAuthorize("@restaurantAuthorizationService.isRestaurantOwner(authentication, #restaurantId)")
//    @GetMapping("/{restaurantId}")
//    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable Long restaurantId) {
//        return ResponseEntity.ok(transactionService.getTransactionsByRestaurant(restaurantId));
//    }
//}

package com.example.restaurantservice.controller;

import com.example.restaurantservice.model.Transaction;
import com.example.restaurantservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurant-transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Get transactions from local database
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable Long restaurantId) {
        log.info("Getting transactions for restaurant ID: {}", restaurantId);
        List<Transaction> transactions = transactionService.getTransactionsForRestaurant(restaurantId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Sync transactions from payment service and save to local database
     */
    @PostMapping("/sync/{restaurantId}")
    public ResponseEntity<?> syncTransactions(@PathVariable Long restaurantId) {
        log.info("Syncing transactions for restaurant ID: {}", restaurantId);
        try {
            List<Transaction> newTransactions = transactionService.syncTransactionsForRestaurant(restaurantId);
            log.info("Synced {} new transactions for restaurant ID: {}", newTransactions.size(), restaurantId);
            return ResponseEntity.ok(newTransactions);
        } catch (Exception e) {
            log.error("Error syncing transactions: {} - {}", e.getClass().getName(), e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Error syncing transactions: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}