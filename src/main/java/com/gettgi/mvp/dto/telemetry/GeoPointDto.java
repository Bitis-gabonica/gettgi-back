package com.gettgi.mvp.dto.telemetry;

/**
 * Represents a WGS84 geographic coordinate.
 */
public record GeoPointDto(double latitude, double longitude) {

    public GeoPointDto {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("latitude must be between -90 and 90 degrees");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("longitude must be between -180 and 180 degrees");
        }
    }
}

