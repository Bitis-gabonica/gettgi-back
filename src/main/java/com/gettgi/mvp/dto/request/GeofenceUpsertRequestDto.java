package com.gettgi.mvp.dto.request;

import com.gettgi.mvp.dto.telemetry.GeoPointDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record GeofenceUpsertRequestDto(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Valid GeoPointDto center,
        @NotNull @Positive @Max(100000) Double radiusMeters
) {
}

