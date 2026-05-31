package com.org.livelocationsharing.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures the WebSocket message broker using STOMP protocol.
 *
 * - Clients connect at /ws (with SockJS fallback)
 * - Messages to /app/... are routed to @MessageMapping methods
 * - Messages to /topic/... are broadcast via the in-memory broker
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker handles /topic subscriptions
        registry.enableSimpleBroker("/topic");
        // Client-to-server messages must be prefixed with /app
        registry.setApplicationDestinationPrefixes("/app");
        log.info("WebSocket message broker configured: simpleBroker=/topic, appPrefix=/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        log.info("STOMP endpoint registered at /ws with SockJS fallback");
    }
}
