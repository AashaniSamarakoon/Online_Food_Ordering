package com.delivery.orderassignmentservice.exception;

public class NoAvailableDriversException extends RuntimeException {
    public NoAvailableDriversException(String message) {
        super(message);
    }
}
