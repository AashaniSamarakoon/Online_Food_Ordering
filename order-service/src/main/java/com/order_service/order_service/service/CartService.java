package com.order_service.order_service.service;

import com.order_service.order_service.client.RestaurantClient;
import com.order_service.order_service.dto.FoodItemResponse;
import com.order_service.order_service.dto.RestaurantResponse;
import com.order_service.order_service.model.Cart;
import com.order_service.order_service.model.CartItem;
import com.order_service.order_service.repository.CartItemRepository;
import com.order_service.order_service.repository.CartRepository;
import com.order_service.order_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestaurantClient restaurantClient;
    private final JwtUtil jwtUtil;

    public Cart getCart(String token) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        return cartRepository.findByUsername(username).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .username(username)
                    .items(new ArrayList<>()) // ✅ explicitly initialize
                    .build();
            return cartRepository.save(newCart);
        });
    }

    public Cart addItemToCart(String token, Long restaurantId, Long foodItemId, int quantity) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        Cart cart = getCart(token);

        RestaurantResponse restaurant = restaurantClient.getRestaurantById(restaurantId);
        FoodItemResponse item = restaurant.getItems().stream()
                .filter(i -> i.getId().equals(foodItemId) && i.isAvailable())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found or unavailable"));

        CartItem cartItem = CartItem.builder()
                .foodItemId(foodItemId)
                .name(item.getName())
                .price(item.getPrice())
                .quantity(quantity)
                .restaurantId(restaurantId)
                .cart(cart)
                .build();

        cart.getItems().add(cartItem);
        cartRepository.save(cart); // cascade saves the item
        return cart;
    }

    public void removeItemFromCart(String token, Long cartItemId) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        Cart cart = getCart(token);

        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cartRepository.save(cart);
    }

    public void clearCart(String token) {
        Cart cart = getCart(token);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
