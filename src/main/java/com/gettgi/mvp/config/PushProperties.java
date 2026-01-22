package com.gettgi.mvp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.push")
public class PushProperties {

    private final Fcm fcm = new Fcm();

    @Getter
    @Setter
    public static class Fcm {
        private boolean enabled = false;
        private boolean dryRun = false;
        private String credentialsPath;
    }
}

