package com.gettgi.mvp.dto.response;

import com.gettgi.mvp.entity.enums.Espece;
import com.gettgi.mvp.entity.enums.Role;
import com.gettgi.mvp.entity.enums.Sexe;
import com.gettgi.mvp.entity.enums.Statut;

import java.util.UUID;

public record FindAllAnimalResponseDto(
        UUID id,
        Espece espece,
        String nom,
        Sexe sexe,
        Statut statut,
        Role role,
        UUID troupeauId,
        int age,
        Float poids
) {
}
