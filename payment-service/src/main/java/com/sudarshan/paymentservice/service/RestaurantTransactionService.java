package com.sudarshan.paymentservice.service;

import com.sudarshan.paymentservice.entity.RestaurantTransaction;

import java.time.LocalDate;
import java.util.List;

public interface RestaurantTransactionService {

    List<RestaurantTransaction> getTransactionsByRestaurantId(Long restaurantId);

    List<RestaurantTransaction> getTransactionsByRestaurantIdAndDate(Long restaurantId, LocalDate date);
}
