package com.delivery.driverauthservice.service;

import com.delivery.driverauthservice.repository.DriverCredentialRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class SequenceGenerator {

    @Value("${app.id-sequence.start:1000000}")
    private long startValue;

    private final DriverCredentialRepository driverCredentialRepository;
    private AtomicLong counter;

    @PostConstruct
    public void init() {
        // Find the highest driver ID in the database
        Long maxId = driverCredentialRepository.findMaxDriverId();

        // If there are existing records, start from the highest ID + 1
        // Otherwise, use the configured start value
        long initialValue = maxId != null && maxId >= startValue
                ? maxId + 1
                : startValue;

        counter = new AtomicLong(initialValue);
        log.info("SequenceGenerator initialized with starting value: {}", initialValue);
    }

    public Long nextId() {
        return counter.getAndIncrement();
    }
}