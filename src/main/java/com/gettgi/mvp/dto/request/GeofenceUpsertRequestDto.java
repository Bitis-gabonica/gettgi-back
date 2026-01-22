package com.gettgi.mvp.dto.request;

import com.gettgi.mvp.dto.telemetry.GeoPointDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record GeofenceUpsertRequestDto(
        @NotBlank @Size(max = 100) String name,
        @NotNull GeoPointDto center,
        @NotNull @Positive Double radiusMeters
) {
}

