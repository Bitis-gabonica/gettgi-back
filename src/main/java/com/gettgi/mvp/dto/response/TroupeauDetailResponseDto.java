package com.gettgi.mvp.dto.response;

import java.util.List;
import java.util.UUID;

public record TroupeauDetailResponseDto(
        UUID id,
        String nom,
        List<FindAllAnimalResponseDto> animals
) {}

