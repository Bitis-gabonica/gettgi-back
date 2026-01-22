package com.gettgi.mvp.security;

import com.gettgi.mvp.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginRateLimiter {

    private final SecurityProperties securityProperties;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null) {
            return false;
        }
        if (attempt.blockedUntil != null && attempt.blockedUntil.isAfter(Instant.now())) {
            return true;
        }
        if (attempt.blockedUntil != null && attempt.blockedUntil.isBefore(Instant.now())) {
            attempts.remove(key);
        }
        return false;
    }

    public void recordFailure(String key) {
        SecurityProperties.Auth auth = securityProperties.getAuth();
        attempts.compute(key, (k, existing) -> {
            Instant now = Instant.now();
            if (existing == null || existing.firstAttempt.plus(auth.getLoginWindow()).isBefore(now)) {
                return new Attempt(1, now, null);
            }
            int updatedAttempts = existing.attempts + 1;
            if (updatedAttempts >= auth.getLoginMaxAttempts()) {
                Instant blockedUntil = now.plus(auth.getLoginLockTime());
                log.warn("Login rate limiter triggered for key={}, locked until {}", key, blockedUntil);
                return new Attempt(updatedAttempts, existing.firstAttempt, blockedUntil);
            }
            return new Attempt(updatedAttempts, existing.firstAttempt, existing.blockedUntil);
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private record Attempt(int attempts, Instant firstAttempt, Instant blockedUntil) {
    }
}
