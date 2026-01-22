package com.gettgi.mvp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.telemetry.mqtt")
public class TelemetryMqttProperties {

    /**
     * MQTT broker URI, e.g. tcp://localhost:1883.
     */
    private String brokerUrl;

    /**
     * Client identifier used when connecting to the broker.
     */
    private String clientId;

    /**
     * Optional username for authenticated brokers.
     */
    private String username;

    /**
     * Optional password for authenticated brokers.
     */
    private String password;

    /**
     * Topics to subscribe to (supports MQTT wildcards).
     */
    private List<String> topics = new ArrayList<>();

    /**
     * Quality of service level (0, 1 or 2).
     */
    private int qos = 1;

    /**
     * Timeout applied when waiting for MQTT acknowledgements.
     */
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration completionTimeout = Duration.ofMillis(5_000);

    /**
     * Whether the inbound adapter should start automatically on application startup.
     */
    private boolean autoStartup = true;

    public List<String> getTopics() {
        return topics == null ? List.of() : List.copyOf(topics);
    }

    public void setTopics(List<String> topics) {
        this.topics = topics == null ? new ArrayList<>() : new ArrayList<>(topics);
    }
}

