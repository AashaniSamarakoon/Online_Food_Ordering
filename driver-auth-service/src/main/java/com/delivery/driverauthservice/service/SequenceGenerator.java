package com.delivery.driverauthservice.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class SequenceGenerator {
    private final AtomicLong counter = new AtomicLong(1_000_000L); // Start from 1,000,000

    public Long nextId() {
        return counter.getAndIncrement();
    }
}