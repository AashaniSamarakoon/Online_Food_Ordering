package com.sudarshan.paymentservice.exceptions;

public class StripeSessionCreationException extends RuntimeException {
    public StripeSessionCreationException(String message) {
        super(message);
    }
}
