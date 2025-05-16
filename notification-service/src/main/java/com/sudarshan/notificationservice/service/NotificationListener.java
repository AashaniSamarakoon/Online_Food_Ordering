package com.sudarshan.notificationservice.service;

import com.sudarshan.notificationservice.dto.NotificationEvent;
import com.sudarshan.notificationservice.entity.Notification;
import com.sudarshan.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationRepository repository;

    @KafkaListener(topics = "notifications", groupId = "notification-service")
    public void consume(NotificationEvent event) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setRole(event.getRole());
        notification.setTitle(event.getTitle());
        notification.setMessage(event.getMessage());
        notification.setCreatedAt(LocalDateTime.now());

        repository.save(notification);
        System.out.println("Notification saved: " + event.getTitle());
    }
}