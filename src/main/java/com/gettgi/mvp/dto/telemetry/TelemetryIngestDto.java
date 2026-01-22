package com.gettgi.mvp.dto.telemetry;

import com.gettgi.mvp.entity.enums.StatusCollar;
import com.gettgi.mvp.entity.enums.StatutTransmission;

import java.time.Instant;
import java.util.Optional;

/**
 * Payload structure used when ingesting a telemetry frame coming from the device via MQTT.
 */
public record TelemetryIngestDto(
        String deviceImei,
        Instant timestamp,
        GeoPointDto position,
        Double speed,
        Double accelX,
        Double accelY,
        Double accelZ,
        Double pressure,
        Integer batteryLevel,
        Integer gsmSignal,
        StatusCollar statusCollar,
        StatutTransmission transmissionStatus
) {

    public TelemetryIngestDto {
        if (deviceImei == null || deviceImei.isBlank()) {
            throw new IllegalArgumentException("deviceImei is required");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp is required");
        }
        if (position == null) {
            throw new IllegalArgumentException("position is required");
        }
    }

    public Optional<Double> speedOptional() {
        return Optional.ofNullable(speed);
    }

    public Optional<Double> accelXOptional() {
        return Optional.ofNullable(accelX);
    }

    public Optional<Double> accelYOptional() {
        return Optional.ofNullable(accelY);
    }

    public Optional<Double> accelZOptional() {
        return Optional.ofNullable(accelZ);
    }

    public Optional<Double> pressureOptional() {
        return Optional.ofNullable(pressure);
    }

    public Optional<Integer> batteryLevelOptional() {
        return Optional.ofNullable(batteryLevel);
    }

    public Optional<Integer> gsmSignalOptional() {
        return Optional.ofNullable(gsmSignal);
    }

    public Optional<StatusCollar> statusCollarOptional() {
        return Optional.ofNullable(statusCollar);
    }

    public Optional<StatutTransmission> transmissionStatusOptional() {
        return Optional.ofNullable(transmissionStatus);
    }
}
