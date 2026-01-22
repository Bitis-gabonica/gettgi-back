package com.gettgi.mvp.security;

import com.gettgi.mvp.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginRateLimiter {

    private static final String ATTEMPTS_KEY_PREFIX = "rate_limit:attempts:";
    private static final String BLOCKED_KEY_PREFIX = "rate_limit:blocked:";

    private final SecurityProperties securityProperties;
    private final RedisTemplate<String, String> redisTemplate;

    public boolean isBlocked(String key) {
        try {
            String blockedKey = BLOCKED_KEY_PREFIX + key;
            String blockedValue = redisTemplate.opsForValue().get(blockedKey);
            
            if (blockedValue != null) {
                // Key exists and hasn't expired (Redis TTL handles expiration)
                log.debug("Login blocked for key={}", key);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error checking if key is blocked: {}", key, e);
            // Fallback: don't block if Redis is unavailable
            return false;
        }
    }

    public void recordFailure(String key) {
        try {
            SecurityProperties.Auth auth = securityProperties.getAuth();
            String attemptsKey = ATTEMPTS_KEY_PREFIX + key;
            String blockedKey = BLOCKED_KEY_PREFIX + key;
            
            // Increment attempt counter atomically
            Long attemptCount = redisTemplate.opsForValue().increment(attemptsKey);
            
            if (attemptCount == null) {
                log.warn("Failed to increment attempt counter for key={}", key);
                return;
            }
            
            // Set TTL on first attempt (loginWindow duration)
            if (attemptCount == 1) {
                long ttlSeconds = auth.getLoginWindow().getSeconds();
                redisTemplate.expire(attemptsKey, ttlSeconds, TimeUnit.SECONDS);
                log.debug("First failed attempt for key={}, TTL set to {} seconds", key, ttlSeconds);
            }
            
            // Check if we've reached the max attempts threshold
            if (attemptCount >= auth.getLoginMaxAttempts()) {
                // Create blocked key with lock time as TTL
                long lockTimeSeconds = auth.getLoginLockTime().getSeconds();
                String timestamp = String.valueOf(Instant.now().getEpochSecond());
                redisTemplate.opsForValue().set(blockedKey, timestamp, lockTimeSeconds, TimeUnit.SECONDS);
                
                // Remove the attempts counter
                redisTemplate.delete(attemptsKey);
                
                log.warn("Login rate limiter triggered for key={}, locked for {} seconds", key, lockTimeSeconds);
            } else {
                log.debug("Failed attempt {} for key={} (max: {})", attemptCount, key, auth.getLoginMaxAttempts());
            }
        } catch (Exception e) {
            log.error("Error recording failure for key={}", key, e);
            // Don't throw exception - allow login to proceed if Redis is unavailable
        }
    }

    public void recordSuccess(String key) {
        try {
            String attemptsKey = ATTEMPTS_KEY_PREFIX + key;
            String blockedKey = BLOCKED_KEY_PREFIX + key;
            
            // Remove both keys on successful login
            redisTemplate.delete(attemptsKey);
            redisTemplate.delete(blockedKey);
            
            log.debug("Successfully cleared rate limit for key={}", key);
        } catch (Exception e) {
            log.error("Error recording success for key={}", key, e);
            // Don't throw exception - allow login to proceed if Redis is unavailable
        }
    }
}
