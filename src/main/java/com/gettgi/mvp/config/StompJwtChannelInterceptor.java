package com.gettgi.mvp.config;

import com.gettgi.mvp.service.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(command) || StompCommand.STOMP.equals(command)) {
            accessor.setUser(authenticate(accessor));
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            requireAuthenticated(accessor);
            String destination = accessor.getDestination();
            if (!isAllowedSubscription(destination)) {
                throw new AccessDeniedException("Subscription not allowed: " + destination);
            }
            return message;
        }

        if (StompCommand.SEND.equals(command) || StompCommand.UNSUBSCRIBE.equals(command)) {
            requireAuthenticated(accessor);
        }

        return message;
    }

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;

    private UsernamePasswordAuthenticationToken authenticate(StompHeaderAccessor accessor) {
        String header = firstNonBlankHeader(accessor, "Authorization", "authorization", "token", "X-Authorization");
        if (!StringUtils.hasText(header)) {
            throw new AccessDeniedException("Missing Authorization header");
        }

        String token = header.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (!StringUtils.hasText(token)) {
            throw new AccessDeniedException("Missing JWT token");
        }

        try {
            String telephone = jwtUtils.extractTelephone(token);
            if (!StringUtils.hasText(telephone)) {
                throw new AccessDeniedException("Invalid JWT token");
            }

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(telephone);
            if (!jwtUtils.validateToken(token, userDetails)) {
                throw new AccessDeniedException("Invalid JWT token");
            }

            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } catch (JwtException ex) {
            log.debug("WS auth failed: invalid JWT token", ex);
            throw new AccessDeniedException("Invalid JWT token", ex);
        }
    }

    private void requireAuthenticated(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            throw new AccessDeniedException("Not authenticated");
        }
    }

    private String firstNonBlankHeader(StompHeaderAccessor accessor, String... headerNames) {
        if (headerNames == null) return null;
        for (String name : headerNames) {
            if (!StringUtils.hasText(name)) continue;
            String value = accessor.getFirstNativeHeader(name);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean isAllowedSubscription(String destination) {
        if (!StringUtils.hasText(destination)) {
            return false;
        }
        if (destination.equals("/user/queue/alerts")) {
            return true;
        }
        if (destination.equals("/user/queue/animals")) {
            return true;
        }
        return destination.startsWith("/user/queue/animals/");
    }
}
