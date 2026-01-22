package com.gettgi.mvp.dto.request;

import java.util.UUID;

public record AnimalTroupeauPatchRequestDto(
        UUID troupeauId // nullable: null to detach from troupeau
) {}

