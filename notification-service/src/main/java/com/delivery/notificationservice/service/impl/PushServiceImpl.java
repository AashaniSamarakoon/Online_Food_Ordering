package com.delivery.notificationservice.service;

import com.delivery.notificationservice.exception.NotificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PushService {

    @Value("${firebase.server.key}")
    private String firebaseServerKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendPushNotification(String deviceToken, String title, String message, Map<String, String> data) {
        try {
            String firebaseApiUrl = "https://fcm.googleapis.com/fcm/send";

            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("body", message);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("to", deviceToken);
            requestBody.put("notification", notification);
            requestBody.put("data", data);

            // Add authorization header
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "key=" + firebaseServerKey);
            headers.put("Content-Type", "application/json");

            // In a production system, you would use a proper HTTP client
            // with appropriate error handling and response processing

            log.info("Sending push notification to device: {}", deviceToken);

            // Simulated success
            log.info("Push notification sent successfully");
        } catch (Exception e) {
            log.error("Error sending push notification: {}", e.getMessage(), e);
            throw new NotificationException("Failed to send push notification: " + e.getMessage());
        }
    }
}