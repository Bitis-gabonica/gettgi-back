package com.gettgi.mvp.dto.telemetry;

import com.gettgi.mvp.dto.telemetry.validation.ValidLatitude;
import com.gettgi.mvp.dto.telemetry.validation.ValidLongitude;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a WGS84 geographic coordinate.
 */
public record GeoPointDto(
        @NotNull
        @ValidLatitude
        Double latitude,
        
        @NotNull
        @ValidLongitude
        Double longitude
) {
    // Compact constructor for additional validation (defense in depth)
    public GeoPointDto {
        if (latitude != null && (latitude < -90.0 || latitude > 90.0)) {
            throw new IllegalArgumentException("latitude must be between -90 and 90 degrees");
        }
        if (longitude != null && (longitude < -180.0 || longitude > 180.0)) {
            throw new IllegalArgumentException("longitude must be between -180 and 180 degrees");
        }
    }
}

