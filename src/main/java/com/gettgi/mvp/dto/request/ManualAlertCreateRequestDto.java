package com.gettgi.mvp.dto.request;

import com.gettgi.mvp.entity.enums.TypeAlerte;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ManualAlertCreateRequestDto(
        @NotNull UUID animalId,
        @NotNull TypeAlerte type,
        @Size(max = 512) String message
) {
}

