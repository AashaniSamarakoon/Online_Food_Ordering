package com.order_service.order_service.service;

import com.order_service.order_service.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final SmsService smsService;

    public void sendOrderConfirmation(Map<String, Object> userProfile, Order order) {
        String email = (String) userProfile.get("email");
        String phoneNumber = (String) userProfile.get("phoneNumber");
        String firstName = (String) userProfile.get("firstName");

        sendEmail(email, firstName, order);
        smsService.sendSms(phoneNumber, buildSmsMessage(firstName, order));
    }

    private void sendEmail(String toEmail, String name, Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Order Confirmation");
        message.setText("Hi " + name + ",\n\n" +
                "Your order #" + order.getId() + " has been placed successfully!\n" +
                "Total: $" + order.getTotalPrice() + "\n" +
                "We will deliver it soon. 🚀\n\n" +
                "Thank you for choosing us!");

        mailSender.send(message);
    }

    private String buildSmsMessage(String name, Order order) {
        return "Hi " + name + ", your order #" + order.getId() + " was placed successfully! Total: $" + order.getTotalPrice();
    }
}

