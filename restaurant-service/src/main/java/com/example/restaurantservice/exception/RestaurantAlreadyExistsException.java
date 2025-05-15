package com.example.restaurantservice.exception;

public class RestaurantAlreadyExistsException extends RuntimeException {
    public RestaurantAlreadyExistsException(String message) {
        super(message);
    }

    public RestaurantAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}