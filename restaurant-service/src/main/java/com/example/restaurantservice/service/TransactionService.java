package com.example.restaurantservice.service;

import com.example.restaurantservice.client.PaymentServiceClient;
import com.example.restaurantservice.dto.TransactionResponse;
import com.example.restaurantservice.model.Transaction;
import com.example.restaurantservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final PaymentServiceClient paymentServiceClient;
    private final TransactionRepository transactionRepository;

    // Sync and save all transactions for a restaurant from payment-service
    @Transactional
    public List<Transaction> syncTransactionsFromPaymentService(Long restaurantId) {
        List<TransactionResponse> remoteList = paymentServiceClient.getTransactionsByRestaurantId(restaurantId);
        List<Transaction> transactions = remoteList.stream().map(dto -> Transaction.builder()
                .id(dto.getId())
                .restaurantId(dto.getRestaurantId())
                .date(LocalDate.parse(dto.getDate()))
                .description(dto.getDescription())
                .bankName(dto.getBankName())
                .amount(dto.getAmount())
                .build()
        ).collect(Collectors.toList());

        // Save all to DB (will update if ID already exists)
        return transactionRepository.saveAll(transactions);
    }

    public List<Transaction> getTransactionsByRestaurant(Long restaurantId) {
        return transactionRepository.findByRestaurantId(restaurantId);
    }
}