package com.order_service.order_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    public void sendSms(String phoneNumber, String message) {

        log.info("Sending SMS to {}: {}", phoneNumber, message);


    }
}

