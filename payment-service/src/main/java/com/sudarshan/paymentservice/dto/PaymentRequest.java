package com.sudarshan.paymentservice.dto;


import com.sudarshan.paymentservice.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    private Long orderId;
    private Long restaurantId;
    private Long riderId;

    private Long totalAmount;       // This includes mealPrice + deliveryCharge
    private Long mealPrice;
    private Long deliveryCharge;
    private Integer numberOfMeals;

    private String mealName;

    private Currency currency;
}
