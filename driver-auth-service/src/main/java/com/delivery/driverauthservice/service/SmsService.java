package com.delivery.driverauthservice.service;

public interface SmsService {
    boolean sendSms(String phoneNumber, String message);
}