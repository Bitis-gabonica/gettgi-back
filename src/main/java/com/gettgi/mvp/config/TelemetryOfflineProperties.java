package com.gettgi.mvp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.telemetry.offline")
public class TelemetryOfflineProperties {

    /**
     * Consider a tracker offline when the last telemetry point is older than this threshold.
     */
    private Duration threshold = Duration.ofMinutes(5);

    /**
     * How often we check for offline trackers.
     */
    private Duration checkInterval = Duration.ofMinutes(1);
}

