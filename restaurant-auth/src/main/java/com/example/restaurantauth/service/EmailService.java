//package com.example.restaurantauth.service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.MailException;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.context.Context;
//import org.springframework.mail.SimpleMailMessage;
//
//import jakarta.annotation.PostConstruct;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//    private final TemplateEngine templateEngine;
//
//    @Value("${spring.mail.username}")
//    private String fromEmail;
//
//    @Value("${super.admin.email:admin@example.com}")
//    private String superAdminEmail;
//
//    @PostConstruct
//    public void init() {
//        // Test email connectivity on startup
//        testEmailConnection();
//    }
//
//    private void testEmailConnection() {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom(fromEmail);
//            message.setTo(fromEmail); // Send to yourself as a test
//            message.setSubject("Email Service Test");
//            message.setText("This is a test email to verify email configuration works.");
//            mailSender.send(message);
//            log.info("Email test connection successful");
//        } catch (MailException e) {
//            log.error("Email test connection failed: {}", e.getMessage(), e);
//            // Don't throw exception to allow application to start
//        }
//    }
//
//    public void sendVerificationEmail(String toEmail, String name, String verificationLink) {
//        if (toEmail == null || toEmail.isEmpty()) {
//            log.error("Recipient email is null or empty");
//            throw new IllegalArgumentException("Recipient email cannot be null or empty");
//        }
//
//        if (verificationLink == null || verificationLink.isEmpty()) {
//            log.error("Verification link is null or empty");
//            throw new IllegalArgumentException("Verification link cannot be null or empty");
//        }
//
//        try {
//            log.info("Preparing verification email - Recipient: {}, Name: {}", toEmail, name);
//
//            Context context = new Context();
//            context.setVariable("name", name);
//            context.setVariable("verificationLink", verificationLink);
//
//            log.debug("Processing email template 'verification-email'");
//            String htmlContent = templateEngine.process("verification-email", context);
//
//            if (htmlContent == null || htmlContent.isEmpty()) {
//                log.error("Email template 'verification-email' rendered empty content");
//                throw new RuntimeException("Email template rendered empty content");
//            }
//
//            log.debug("Email content generated successfully, attempting to send email");
//            sendHtmlEmail(toEmail, "Verify Your Restaurant Account", htmlContent);
//            log.info("Verification email sent successfully to: {}", toEmail);
//        } catch (MessagingException e) {
//            log.error("Failed to send verification email to {} due to messaging error: {}", toEmail, e.getMessage(), e);
//            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
//        } catch (Exception e) {
//            log.error("Unexpected error while sending verification email to {}: {}", toEmail, e.getMessage(), e);
//            throw new RuntimeException("Failed to send verification email due to unexpected error", e);
//        }
//    }
//
//    public void notifySuperAdmin(String newRestaurantName, String ownerName, String ownerEmail) {
//        try {
//            log.info("Preparing to send admin notification for: {}", newRestaurantName);
//
//            Context context = new Context();
//            context.setVariable("restaurantName", newRestaurantName);
//            context.setVariable("ownerName", ownerName);
//            context.setVariable("ownerEmail", ownerEmail);
//
//            String htmlContent = templateEngine.process("admin-notification", context);
//
//            if (htmlContent == null || htmlContent.isEmpty()) {
//                log.error("Email template 'admin-notification' rendered empty content");
//                return;
//            }
//
//            sendHtmlEmail(superAdminEmail, "New Restaurant Registration", htmlContent);
//            log.info("Super admin notification sent successfully for: {}", newRestaurantName);
//        } catch (Exception e) {
//            log.error("Failed to send admin notification for {}: {}", newRestaurantName, e.getMessage(), e);
//            // Don't throw exception here as this is a notification only
//        }
//    }
//
//    public void sendSimpleEmail(String to, String subject, String text) {
//        try {
//            log.info("Sending simple email to: {}", to);
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom(fromEmail);
//            message.setTo(to);
//            message.setSubject(subject);
//            message.setText(text);
//            mailSender.send(message);
//            log.info("Simple email sent successfully to: {}", to);
//        } catch (MailException e) {
//            log.error("Failed to send simple email to {}: {}", to, e.getMessage(), e);
//        }
//    }
//
//    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
//        if (mailSender == null) {
//            log.error("JavaMailSender is null - mail configuration may be incorrect");
//            throw new MessagingException("Mail sender is not properly configured");
//        }
//
//        try {
//            log.debug("Creating MimeMessage for recipient: {}", to);
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            log.debug("Setting email parameters - From: {}, To: {}, Subject: {}", fromEmail, to, subject);
//            helper.setFrom(fromEmail);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(htmlContent, true);
//
//            log.debug("Attempting to send HTML email to: {}", to);
//            mailSender.send(message);
//            log.info("HTML email successfully sent to: {}", to);
//        } catch (MessagingException e) {
//            log.error("MessagingException when sending HTML email to {}: {}", to, e.getMessage(), e);
//            throw e;
//        } catch (Exception e) {
//            log.error("Unexpected error when sending HTML email to {}: {}", to, e.getMessage(), e);
//            throw new MessagingException("Failed to send HTML email: " + e.getMessage(), e);
//        }
//    }
//}
package com.example.restaurantauth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.mail.SimpleMailMessage;

import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${super.admin.email:admin@example.com}")
    private String superAdminEmail;

    @Value("${app.base-url:http://localhost:8082}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        // Test email connectivity on startup
        testEmailConnection();
    }

    private void testEmailConnection() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(fromEmail); // Send to yourself as a test
            message.setSubject("Email Service Test");
            message.setText("This is a test email to verify email configuration works.");
            mailSender.send(message);
            log.info("Email test connection successful");
        } catch (MailException e) {
            log.error("Email test connection failed: {}", e.getMessage(), e);
            // Don't throw exception to allow application to start
        }
    }

    public void sendVerificationEmail(String toEmail, String name, String verificationLink) {
        if (toEmail == null || toEmail.isEmpty()) {
            log.error("Recipient email is null or empty");
            throw new IllegalArgumentException("Recipient email cannot be null or empty");
        }

        if (verificationLink == null || verificationLink.isEmpty()) {
            log.error("Verification link is null or empty");
            throw new IllegalArgumentException("Verification link cannot be null or empty");
        }

        try {
            log.info("Preparing verification email - Recipient: {}, Name: {}", toEmail, name);

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("verificationLink", verificationLink);

            log.debug("Processing email template 'verification-email'");
            String htmlContent = templateEngine.process("verification-email", context);

            if (htmlContent == null || htmlContent.isEmpty()) {
                log.error("Email template 'verification-email' rendered empty content");
                throw new RuntimeException("Email template rendered empty content");
            }

            log.debug("Email content generated successfully, attempting to send email");
            sendHtmlEmail(toEmail, "Verify Your Restaurant Account", htmlContent);
            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {} due to messaging error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while sending verification email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email due to unexpected error", e);
        }
    }

    public void notifySuperAdmin(String newRestaurantName, String ownerName, String ownerEmail) {
        try {
            log.info("Preparing to send admin notification for: {}", newRestaurantName);

            // Create direct verification link
            String verifyLink = baseUrl + "/api/auth/admin-verify?email=" + ownerEmail;

            // Admin panel link
            String adminPanelLink = baseUrl + "/admin/dashboard";

            Context context = new Context();
            context.setVariable("restaurantName", newRestaurantName);
            context.setVariable("ownerName", ownerName);
            context.setVariable("ownerEmail", ownerEmail);
            context.setVariable("verifyLink", verifyLink);
            context.setVariable("adminPanelLink", adminPanelLink);

            String htmlContent = templateEngine.process("admin-notification", context);

            if (htmlContent == null || htmlContent.isEmpty()) {
                log.error("Email template 'admin-notification' rendered empty content");
                return;
            }

            sendHtmlEmail(superAdminEmail, "New Restaurant Registration", htmlContent);
            log.info("Super admin notification sent successfully for: {}", newRestaurantName);
        } catch (Exception e) {
            log.error("Failed to send admin notification for {}: {}", newRestaurantName, e.getMessage(), e);
            // Don't throw exception here as this is a notification only
        }
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            log.info("Sending simple email to: {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (MailException e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage(), e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        if (mailSender == null) {
            log.error("JavaMailSender is null - mail configuration may be incorrect");
            throw new MessagingException("Mail sender is not properly configured");
        }

        try {
            log.debug("Creating MimeMessage for recipient: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            log.debug("Setting email parameters - From: {}, To: {}, Subject: {}", fromEmail, to, subject);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            log.debug("Attempting to send HTML email to: {}", to);
            mailSender.send(message);
            log.info("HTML email successfully sent to: {}", to);
        } catch (MessagingException e) {
            log.error("MessagingException when sending HTML email to {}: {}", to, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when sending HTML email to {}: {}", to, e.getMessage(), e);
            throw new MessagingException("Failed to send HTML email: " + e.getMessage(), e);
        }
    }
}