package com.delivery.orderassignmentservice.exception;

public class AssignmentNotFoundException extends RuntimeException {
    public AssignmentNotFoundException(String message) {
        super(message);
    }
}