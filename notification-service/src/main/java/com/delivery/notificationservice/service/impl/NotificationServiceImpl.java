package com.delivery.notificationservice.service;

import com.delivery.notificationservice.client.UserServiceClient;
import com.delivery.notificationservice.dto.NotificationResponseDTO;
import com.delivery.notificationservice.model.*;
import com.delivery.notificationservice.repository.NotificationRepository;
import com.delivery.notificationservice.repository.UserPreferenceRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final SmsService smsService;
    private final PushService pushService;
    private final TemplateService templateService;
    private final NotificationRepository notificationRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public NotificationResponseDTO sendNotificationByTemplate(
            String userId,
            String templateCode,
            NotificationType type,
            Map<String, String> parameters,
            String correlationId,
            String sourceService,
            String sourceEvent,
            String entityType,
            String entityId
    ) {
        // 1. Check user preferences
        UserPreference userPreference = getUserPreferencesOrDefault(userId);

        // 2. Skip if user has opted out of this notification type
        if (isUserOptedOut(userPreference, type.toString())) {
            log.info("User {} has opted out of notifications of type {}", userId, type);
            return NotificationResponseDTO.builder()
                    .status(NotificationStatus.SKIPPED)
                    .message("User opted out")
                    .build();
        }

        // 3. Get recipient details (email, phone, etc.) based on type
        String recipient = getRecipientAddress(userPreference, type);
        if (recipient == null || recipient.isEmpty()) {
            log.warn("No recipient address found for user {} and type {}", userId, type);
            return NotificationResponseDTO.builder()
                    .status(NotificationStatus.FAILED)
                    .message("Missing recipient information")
                    .build();
        }

        // 4. Get template and render content
        NotificationTemplate template = templateService.getTemplate(templateCode, type);
        String renderedContent = templateService.renderTemplate(template, parameters);
        String renderedSubject = templateService.renderSubject(template, parameters);

        // 5. Create notification record
        Notification notification = Notification.builder()
                .recipient(recipient)
                .templateCode(templateCode)
                .type(type)
                .subject(renderedSubject)
                .message(renderedContent)
                .parameters(parameters)
                .status(NotificationStatus.PENDING)
                .correlationId(correlationId)
                .sourceService(sourceService)
                .sourceEvent(sourceEvent)
                .entityType(entityType)
                .entityId(entityId)
                .retryCount(0)
                .build();

        notification = notificationRepository.save(notification);

        // 6. Send notification based on type
        try {
            sendNotification(notification);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);

            return NotificationResponseDTO.builder()
                    .notificationId(notification.getId())
                    .status(NotificationStatus.SENT)
                    .message("Notification sent successfully")
                    .build();
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);

            return NotificationResponseDTO.builder()
                    .notificationId(notification.getId())
                    .status(NotificationStatus.FAILED)
                    .message("Failed to send notification: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    @Retry(name = "notificationRetry")
    @CircuitBreaker(name = "notificationService", fallbackMethod = "handleNotificationFailure")
    public void sendNotification(Notification notification) {
        switch (notification.getType()) {
            case EMAIL -> emailService.sendEmail(
                    notification.getRecipient(),
                    notification.getSubject(),
                    notification.getMessage()
            );
            case SMS -> smsService.sendSms(
                    notification.getRecipient(),
                    notification.getMessage()
            );
            case PUSH -> pushService.sendPushNotification(
                    notification.getRecipient(),
                    notification.getSubject(),
                    notification.getMessage(),
                    notification.getParameters()
            );
            default -> throw new IllegalArgumentException("Unsupported notification type: " + notification.getType());
        }
    }

    private UserPreference getUserPreferencesOrDefault(String userId) {
        return userPreferenceRepository.findById(userId)
                .orElseGet(() -> {
                    // Try to fetch from user service
                    try {
                        return userServiceClient.getUserPreferences(userId);
                    } catch (Exception e) {
                        log.warn("Could not fetch user preferences for user {}: {}", userId, e.getMessage());
                        // Return default preferences
                        return UserPreference.builder()
                                .userId(userId)
                                .build();
                    }
                });
    }

    private boolean isUserOptedOut(UserPreference preferences, String notificationType) {
        return Optional.ofNullable(preferences.getChannelPreferences().get(notificationType))
                .map(enabled -> !enabled)
                .orElse(false); // Default to not opted out if no preference is set
    }

    private String getRecipientAddress(UserPreference preferences, NotificationType type) {
        return switch (type) {
            case EMAIL -> preferences.getEmail();
            case SMS -> preferences.getPhoneNumber();
            case PUSH -> preferences.getDeviceToken();
        };
    }

    private NotificationResponseDTO handleNotificationFailure(Notification notification, Throwable t) {
        log.error("Circuit breaker opened for notification service: {}", t.getMessage());

        notification.setStatus(NotificationStatus.FAILED);
        notification.setErrorMessage("Service unavailable: " + t.getMessage());
        notification.setRetryCount(notification.getRetryCount() + 1);
        notificationRepository.save(notification);

        return NotificationResponseDTO.builder()
                .notificationId(notification.getId())
                .status(NotificationStatus.FAILED)
                .message("Service temporarily unavailable")
                .build();
    }

    @Transactional
    public void updateNotificationStatus(Long notificationId, NotificationStatus status) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setStatus(status);
            if (status == NotificationStatus.DELIVERED) {
                notification.setDeliveredAt(LocalDateTime.now());
            }
            notificationRepository.save(notification);
        });
    }
}