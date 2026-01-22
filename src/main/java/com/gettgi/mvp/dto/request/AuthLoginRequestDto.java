package com.gettgi.mvp.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequestDto(

        @NotBlank
        // TODO validation regex
        String telephone,

        // Optional: allow phone-only login in MVP.
        String password
) {
}
