package com.gettgi.mvp.dto.telemetry;

import com.gettgi.mvp.entity.enums.StatusCollar;
import com.gettgi.mvp.entity.enums.TypeAlerte;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO broadcast to the front-end each time a new telemetry point is available.
 */
public record RealtimePositionDto(
        UUID animalId,
        String animalLabel,
        UUID deviceId,
        String deviceImei,
        GeoPointDto position,
        Double speed,
        Integer batteryLevel,
        Integer gsmSignal,
        StatusCollar collarStatus,
        Instant timestamp,
        boolean insideGeofence,
        UUID geofenceId,
        String geofenceName,
        List<TypeAlerte> activeAlerts
) {

    public RealtimePositionDto {
        if (animalId == null) {
            throw new IllegalArgumentException("animalId is required");
        }
        if (deviceId == null) {
            throw new IllegalArgumentException("deviceId is required");
        }
        if (position == null) {
            throw new IllegalArgumentException("position is required");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp is required");
        }
        activeAlerts = activeAlerts == null ? List.of() : List.copyOf(activeAlerts);
    }
}
