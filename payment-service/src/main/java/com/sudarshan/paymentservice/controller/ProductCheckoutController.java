package com.sudarshan.paymentservice.controller;

import com.sudarshan.paymentservice.dto.PaymentRequest;
import com.sudarshan.paymentservice.dto.StripeResponse;
import com.sudarshan.paymentservice.entity.Payment;
import com.sudarshan.paymentservice.service.StripeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/product/v1")
public class ProductCheckoutController {


    private StripeService stripeService;

    public ProductCheckoutController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<StripeResponse> checkoutProducts(@Valid @RequestBody PaymentRequest paymentRequest) {
        StripeResponse stripeResponse = stripeService.checkoutProducts(paymentRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stripeResponse);
    }

    @GetMapping("/payments/restaurant/{restaurantId}")
    public ResponseEntity<List<Payment>> getPaymentsByRestaurantId(@PathVariable Long restaurantId) {
        List<Payment> payments = stripeService.getPaymentsByRestaurantId(restaurantId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments/rider/{riderId}")
    public ResponseEntity<List<Payment>> getPaymentsByRiderId(@PathVariable Long riderId) {
        List<Payment> payments = stripeService.getPaymentsByRiderId(riderId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments/restaurant/{restaurantId}/rider/{riderId}")
    public ResponseEntity<List<Payment>> getPaymentsByRestaurantAndRider(
            @PathVariable Long restaurantId,
            @PathVariable Long riderId) {
        List<Payment> payments = stripeService.getPaymentsByRestaurantAndRider(restaurantId, riderId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = stripeService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @PatchMapping("/payments/{paymentId}/rider")
    public ResponseEntity<Payment> updateRiderId(
            @PathVariable Long paymentId,
            @RequestParam Long newRiderId) {
        Payment updatedPayment = stripeService.updateRiderId(paymentId, newRiderId);
        return ResponseEntity.ok(updatedPayment);
    }
}