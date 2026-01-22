package com.gettgi.mvp.dto.telemetry;

import com.gettgi.mvp.entity.enums.StatusCollar;
import com.gettgi.mvp.entity.enums.StatutTransmission;

import java.time.Instant;

public record TelemetryPointDto(
        Instant timestamp,
        GeoPointDto position,
        Double speed,
        Double accelX,
        Double accelY,
        Double accelZ,
        Double pressure,
        Integer batteryLevel,
        Integer gsmSignal,
        StatusCollar collarStatus,
        StatutTransmission transmissionStatus
) {
}

