package com.gettgi.mvp.dto.request;

import com.gettgi.mvp.entity.enums.StatusCollar;
import jakarta.validation.constraints.*;

import java.time.Instant;

public record DeviceCreateRequestDto(

        @NotBlank
        @Size(min = 15, max = 15)
        @Pattern(regexp = "\\d{15}", message = "IMEI doit contenir exactement 15 chiffres")
        String imei,

        // optionnels
        @Size(max = 32)
        String firmwareVersion,

        Instant activationDate,

        // facultatif à la création (peut être null)
        StatusCollar statusCollar
) {}