package com.clinicapp.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Enables WebSocket message handling, backed by a message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Configure the message broker options

        // Enable a simple in-memory message broker to carry messages back to the client
        // on destinations prefixed with "/topic" and "/queue"
        // "/topic" is typically used for publish-subscribe (one-to-many)
        // "/queue" is typically used for point-to-point (one-to-one) messaging
        config.enableSimpleBroker("/topic", "/queue");

        // Designates the "/app" prefix for messages that are bound for
        // @MessageMapping-annotated methods in controllers.
        // e.g., a message sent to "/app/chat" will be routed to a controller method mapped to "/chat"
        config.setApplicationDestinationPrefixes("/app");

        // Configure the prefix used for user-specific destinations (e.g., for private messages)
        // Allows sending messages directly to a user's queue like "/user/queue/reply"
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint, enabling SockJS fallback options so that alternate transports
        // may be used if WebSocket is not available. SockJS is recommended for browser compatibility.
        // The endpoint is where clients will connect to the WebSocket server.
        // Allow all origins for now - restrict this in production!
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // TODO: Restrict origins in production!
                .withSockJS();
    }

    // TODO: Add WebSocket security configuration later (e.g., using Spring Security)
    // to ensure only authenticated users can connect and send/receive messages.
}