package com.gettgi.mvp.dto.telemetry;

import com.gettgi.mvp.entity.enums.TypeAlerte;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO streamed to the front-end whenever a telemetry anomaly leads to an alert.
 */
public record AlertNotificationDto(
        UUID alertId,
        UUID animalId,
        UUID deviceId,
        TypeAlerte type,
        String message,
        Instant raisedAt,
        Boolean resolved,
        Instant resolvedAt
) {

    public AlertNotificationDto {
        if (alertId == null) {
            throw new IllegalArgumentException("alertId is required");
        }
        if (animalId == null) {
            throw new IllegalArgumentException("animalId is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
        if (raisedAt == null) {
            throw new IllegalArgumentException("raisedAt is required");
        }
    }
}
