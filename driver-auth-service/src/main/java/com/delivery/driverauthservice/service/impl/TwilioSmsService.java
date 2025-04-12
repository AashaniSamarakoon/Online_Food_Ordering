package com.delivery.driverauthservice.service.impl;

import com.delivery.driverauthservice.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioSmsService implements SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @Value("${app.sms.enabled:true}")
    private boolean smsEnabled;

    @PostConstruct
    private void init() {
        if (smsEnabled) {
            Twilio.init(accountSid, authToken);
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.info("SMS service disabled. Would have sent to {}: {}", phoneNumber, message);
            return true;
        }

        try {
            Message twilioMessage = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    message
            ).create();

            log.info("SMS sent to {} with SID: {}", phoneNumber, twilioMessage.getSid());
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }
}