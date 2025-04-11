package com.delivery.notificationservice.service;

import com.delivery.notificationservice.exception.TemplateNotFoundException;
import com.delivery.notificationservice.model.NotificationTemplate;
import com.delivery.notificationservice.model.NotificationType;
import com.delivery.notificationservice.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");

    @Cacheable("templates")
    public NotificationTemplate getTemplate(String templateCode, NotificationType type) {
        return templateRepository.findByCodeAndType(templateCode, type)
                .orElseThrow(() -> new TemplateNotFoundException(
                        "Template not found with code: " + templateCode + " and type: " + type));
    }

    public String renderTemplate(NotificationTemplate template, Map<String, String> parameters) {
        String content = template.getTemplateContent();
        return replaceParameters(content, parameters);
    }

    public String renderSubject(NotificationTemplate template, Map<String, String> parameters) {
        if (template.getSubject() == null) {
            return "";
        }
        return replaceParameters(template.getSubject(), parameters);
    }

    private String replaceParameters(String template, Map<String, String> parameters) {
        if (template == null || template.isEmpty()) {
            return "";
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String paramName = matcher.group(1).trim();
            String replacement = parameters.getOrDefault(paramName, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}