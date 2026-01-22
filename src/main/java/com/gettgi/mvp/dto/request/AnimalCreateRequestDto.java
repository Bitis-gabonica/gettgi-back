package com.gettgi.mvp.dto.request;

import com.gettgi.mvp.entity.enums.Espece;
import com.gettgi.mvp.entity.enums.Role;
import com.gettgi.mvp.entity.enums.Sexe;
import com.gettgi.mvp.entity.enums.Statut;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record AnimalCreateRequestDto(

        @Min(0)
        int age,

        @NotNull
        Sexe sexe,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        Float taille,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        Float poids,

        @NotNull
        Statut statut,

        // optionnel
        Role role,

        @NotNull
        Espece espece,

        @Size(max = 100)
        String nom,

        @Valid
        @Size(max = 50)
        List<VaccinCreateRequestDto> vaccins,

        // optionnel: création du device au moment de l’ajout
        @Valid
        DeviceCreateRequestDto device


) {}
