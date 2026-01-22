package com.gettgi.mvp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.telemetry.persistence")
public class TelemetryPersistenceProperties {

    /**
     * Interval between two flush operations.
     */
    private Duration flushInterval = Duration.ofSeconds(30);

    /**
     * Maximum number of telemetry points stored per flush.
     * If exceeded, the oldest entries are dropped in favour of the latest.
     */
    private int maxEntries = 5000;
}

