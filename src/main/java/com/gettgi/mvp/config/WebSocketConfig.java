package com.gettgi.mvp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final SecurityProperties securityProperties;
    private final StompJwtChannelInterceptor stompJwtChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompJwtChannelInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] allowedOrigins = resolveAllowedOrigins(securityProperties.getWebsocketAllowedOrigins());
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins);
    }

    private String[] resolveAllowedOrigins(List<String> allowedOrigins) {
        List<String> resolved = new ArrayList<>();
        if (allowedOrigins != null) {
            resolved.addAll(allowedOrigins);
        }

        // Always allow local dev (Flutter web uses random localhost ports).
        resolved.add("http://localhost:[*]");
        resolved.add("http://127.0.0.1:[*]");
        resolved.add("http://localhost:*");
        resolved.add("http://127.0.0.1:*");
        resolved.add("null");

        return resolved.stream().distinct().toArray(String[]::new);
    }
}
