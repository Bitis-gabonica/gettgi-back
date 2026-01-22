package com.gettgi.mvp.dto.response;

import com.gettgi.mvp.entity.enums.UserRole;
import java.util.UUID;

public record AuthRegisterResponseDto(
        UUID id,
        String nom,
        String prenom,
        String telephone,
        String email,
        UserRole role
) {
}
