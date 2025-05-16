package com.order_service.order_service.controller;

import com.order_service.order_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/fee/{restaurantId}")
    public double getDeliveryFee(@RequestHeader("Authorization") String token,
                                 @PathVariable Long restaurantId) {
        return deliveryService.calculateDeliveryFee(token, restaurantId);
    }
}
