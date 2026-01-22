package com.gettgi.mvp.dto.response;
import com.gettgi.mvp.entity.enums.Espece;
import com.gettgi.mvp.entity.enums.Role;
import com.gettgi.mvp.entity.enums.Sexe;
import com.gettgi.mvp.entity.enums.Statut;

import java.util.UUID;

public record AnimalCreateResponseDto(
        UUID id,
        int age,
        Sexe sexe,
        Float taille,
        Float poids,
        Statut statut,
        Role role,
        Espece espece,
        String nom,

        // rattachements
        UUID user


) {}
