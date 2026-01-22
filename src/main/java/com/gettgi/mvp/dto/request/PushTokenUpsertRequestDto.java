package com.gettgi.mvp.dto.request;

import com.gettgi.mvp.entity.enums.PushPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PushTokenUpsertRequestDto(
        @NotBlank @Size(max = 512) String token,
        PushPlatform platform
) {
}
