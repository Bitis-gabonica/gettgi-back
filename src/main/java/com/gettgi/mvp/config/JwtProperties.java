package com.gettgi.mvp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secretKey;
    private List<String> rotationKeys = new ArrayList<>();

    @DurationUnit(ChronoUnit.HOURS)
    private Duration rotationInterval = Duration.ofHours(24);

    private long expirationTime;
    private String issuer = "gettgi-mvp";

    public List<String> allKeys() {
        List<String> keys = new ArrayList<>();
        if (secretKey != null) {
            keys.add(secretKey);
        }
        if (rotationKeys != null) {
            rotationKeys.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .forEach(keys::add);
        }
        return keys;
    }
}
