package com.example.restaurantservice.client;

import com.example.restaurantservice.config.FeignClientConfig;
import com.example.restaurantservice.dto.TransactionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "payment-service", url = "http://localhost:8099", configuration = FeignClientConfig.class)
public interface PaymentServiceClient {
    @GetMapping("/api/transactions/restaurant/{restaurantId}")
    List<TransactionResponse> getTransactionsByRestaurantId(@PathVariable("restaurantId") Long restaurantId);
}