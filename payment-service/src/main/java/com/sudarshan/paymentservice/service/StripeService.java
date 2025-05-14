package com.sudarshan.paymentservice.service;


import com.sudarshan.paymentservice.dto.PaymentRequest;
import com.sudarshan.paymentservice.dto.StripeResponse;
import com.sudarshan.paymentservice.entity.Payment;

import java.util.List;

public interface StripeService {
    StripeResponse checkoutProducts(PaymentRequest paymentRequest);
    void pollStripeForPendingPayments();
    List<Payment> getPaymentsByRestaurantId(Long restaurantId);
    List<Payment> getPaymentsByRiderId(Long riderId);
    List<Payment> getPaymentsByRestaurantAndRider(Long restaurantId, Long riderId);
    List<Payment> getAllPayments();
    Payment updateRiderId(Long paymentId, Long newRiderId);
}
