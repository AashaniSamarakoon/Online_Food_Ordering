package com.sudarshan.paymentservice.exceptions;

public class StripePollingException extends RuntimeException {
    public StripePollingException(String message) {
        super(message);
    }
}
