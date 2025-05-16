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
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestaurantClient restaurantClient;
    private final JwtUtil jwtUtil;

    public Cart getCart(String token) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .userId(userId)
                    .items(new ArrayList<>())
                    .build();
            return cartRepository.save(newCart);
        });
    }

    public Cart addItemToCart(String token, Long restaurantId, Long foodItemId, int quantity) {
        Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        Cart cart = getCart(token);

        RestaurantResponse restaurant = restaurantClient.getRestaurantById(restaurantId);
        FoodItemResponse item = restaurant.getItems().stream()
                .filter(i -> i.getId().equals(foodItemId) && i.isAvailable())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found or unavailable"));

        // Check if item already exists in the cart for the same restaurant
        CartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getFoodItemId().equals(foodItemId) && i.getRestaurantId().equals(restaurantId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // If item already in cart, increment quantity
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            // Otherwise, add new item to the cart
            CartItem cartItem = CartItem.builder()
                    .foodItemId(foodItemId)
                    .name(item.getName())
                    .imageUrl(item.getImageUrl())
                    .price(item.getPrice())
                    .quantity(quantity)
                    .restaurantId(restaurantId)
                    .restaurantName(restaurant.getName())
                    .cart(cart)
                    .build();

            cart.getItems().add(cartItem);
        }

        return cartRepository.save(cart);
    }


//    public void removeItemFromCart(String token, Long cartItemId) {
//        Cart cart = getCart(token);
//        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
//        cartRepository.save(cart);
//    }

    public void removeItemFromCart(String token, Long cartItemId) {
        Cart cart = getCart(token);
        Iterator<CartItem> iterator = cart.getItems().iterator();

        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (item.getId().equals(cartItemId)) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                } else {
                    iterator.remove();
                }
                break;
            }
        }

        cartRepository.save(cart);
    }


    public void clearCart(String token) {
        Cart cart = getCart(token);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}