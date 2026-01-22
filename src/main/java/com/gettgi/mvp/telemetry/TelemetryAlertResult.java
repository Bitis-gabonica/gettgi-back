package com.gettgi.mvp.telemetry;

import com.gettgi.mvp.dto.telemetry.AlertNotificationDto;
import com.gettgi.mvp.entity.enums.TypeAlerte;

import java.util.List;
import java.util.UUID;

public record TelemetryAlertResult(
        boolean insideGeofence,
        UUID geofenceId,
        String geofenceName,
        List<TypeAlerte> activeAlerts,
        List<AlertNotificationDto> notifications
) {

    public TelemetryAlertResult {
        activeAlerts = activeAlerts == null ? List.of() : List.copyOf(activeAlerts);
        notifications = notifications == null ? List.of() : List.copyOf(notifications);
    }
}

