package com.sudarshan.paymentservice.service;

import com.sudarshan.paymentservice.entity.RestaurantTransaction;
import com.sudarshan.paymentservice.repository.RestaurantTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantTransactionServiceImpl implements RestaurantTransactionService {

    private final RestaurantTransactionRepository transactionRepository;

    @Override
    public List<RestaurantTransaction> getTransactionsByRestaurantId(Long restaurantId) {
        return transactionRepository.findByRestaurantId(restaurantId);
    }

    @Override
    public List<RestaurantTransaction> getTransactionsByRestaurantIdAndDate(Long restaurantId, LocalDate date) {
        return transactionRepository.findByRestaurantIdAndDate(restaurantId, date);
    }
}