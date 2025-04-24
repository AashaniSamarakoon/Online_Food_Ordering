package com.delivery.driverauthservice.service.impl;

import com.delivery.driverauthservice.service.SmsService;
import com.messagebird.MessageBirdClient;
import com.messagebird.MessageBirdService;
import com.messagebird.MessageBirdServiceImpl;
import com.messagebird.exceptions.GeneralException;
import com.messagebird.exceptions.UnauthorizedException;
import com.messagebird.objects.MessageResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MessageBirdSmsService implements SmsService {

    @Value("${messagebird.api.key:your_api_key}")
    private String apiKey;

    @Value("${messagebird.originator:YumYum}")
    private String originator;

    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    private MessageBirdClient messageBirdClient;

    @PostConstruct
    private void init() {
        if (smsEnabled) {
            try {
                MessageBirdService messageBirdService = new MessageBirdServiceImpl(apiKey);
                messageBirdClient = new MessageBirdClient(messageBirdService);
                log.info("MessageBird SMS service initialized");
            } catch (Exception e) {
                log.error("Failed to initialize MessageBird: {}", e.getMessage());
                smsEnabled = false;
            }
        } else {
            log.info("SMS service is disabled");
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.info("SMS service disabled. Would have sent to {}: {}", phoneNumber, message);
            return true;
        }

        try {
            // Make sure phone number is in international format (starts with +)
            String formattedPhoneNumber = phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;

            // Create recipients list (MessageBird supports sending to multiple numbers)
            List<BigInteger> recipients = new ArrayList<>();
            recipients.add(new BigInteger(formattedPhoneNumber.substring(1))); // Remove the '+' for BigInteger

            // Send the message
            MessageResponse messageResponse = messageBirdClient.sendMessage(
                    originator,
                    message,
                    recipients
            );

            log.info("SMS sent to {} with ID: {}", phoneNumber, messageResponse.getId());
            return true;
        } catch (UnauthorizedException e) {
            log.error("Authentication failed with MessageBird: {}", e.getMessage(), e);
            return false;
        } catch (GeneralException e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage(), e);
            // Return true anyway to prevent registration failure due to SMS issues
            // You might want to change this behavior depending on your requirements
            return true;
        } catch (Exception e) {
            log.error("Unexpected error when sending SMS to {}: {}", phoneNumber, e.getMessage(), e);
            return true;
        }
    }
}