package com.gettgi.mvp.dto.response;

import com.gettgi.mvp.dto.telemetry.GeoPointDto;

import java.util.UUID;

public record GeofenceResponseDto(
        UUID id,
        String name,
        GeoPointDto center,
        Double radiusMeters,
        GeoPointDto userPosition
) {
}
