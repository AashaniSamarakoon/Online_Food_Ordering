package com.delivery.orderassignmentservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@TestConfiguration
public class TestWebSocketConfig {

    @Bean
    @Primary
    public WebSocketMessageBrokerConfigurer testWebSocketMessageBrokerConfigurer() {
        return new WebSocketMessageBrokerConfigurer() {
            @Override
            public void configureMessageBroker(MessageBrokerRegistry registry) {
                registry.enableSimpleBroker("/topic", "/queue");
                registry.setApplicationDestinationPrefixes("/app");
            }

            @Override
            public void registerStompEndpoints(StompEndpointRegistry registry) {
                registry.addEndpoint("/ws").withSockJS();
            }
        };
    }
}