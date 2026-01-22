package com.gettgi.mvp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TroupeauCreateRequestDto(
        @NotBlank
        @Size(max = 100)
        String nom
) {}

