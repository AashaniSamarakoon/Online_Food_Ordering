package com.sudarshan.paymentservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    private Long restaurantId;
    private Long totalAmount;
    private Long deliveryCharge;
    private Integer numberOfMeals;
    private List<String> mealNames;
}
