package com.gettgi.mvp.dto.response;

import com.gettgi.mvp.entity.enums.StatusCollar;

import java.time.Instant;

public record DeviceDetailResponseDto(
        String imei,
        String firmwareVersion,
        Instant activationDate,
        StatusCollar statusCollar
) {}

