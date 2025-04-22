package com.order_service.order_service.controller;

import com.order_service.order_service.model.Cart;
import com.order_service.order_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(cartService.getCart(token));
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(
            @RequestHeader("Authorization") String token,
            @RequestParam Long restaurantId,
            @RequestParam Long foodItemId,
            @RequestParam int quantity
    ) {
        return ResponseEntity.ok(cartService.addItemToCart(token, restaurantId, foodItemId, quantity));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<Void> removeItem(
            @RequestHeader("Authorization") String token,
            @PathVariable Long itemId
    ) {
        cartService.removeItemFromCart(token, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@RequestHeader("Authorization") String token) {
        cartService.clearCart(token);
        return ResponseEntity.noContent().build();
    }
}
