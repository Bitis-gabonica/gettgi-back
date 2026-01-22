package com.gettgi.mvp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private final Auth auth = new Auth();

    @Getter
    @Setter
    public static class Auth {
        private int loginMaxAttempts = 5;
        private Duration loginWindow = Duration.ofMinutes(5);
        private Duration loginLockTime = Duration.ofMinutes(15);
    }
}
