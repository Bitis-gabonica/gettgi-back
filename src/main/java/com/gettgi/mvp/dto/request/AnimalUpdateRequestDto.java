package com.gettgi.mvp.dto.request;

import com.gettgi.mvp.entity.enums.Espece;
import com.gettgi.mvp.entity.enums.Role;
import com.gettgi.mvp.entity.enums.Sexe;
import com.gettgi.mvp.entity.enums.Statut;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record AnimalUpdateRequestDto(

        @Min(0)
        int age,

        @NotNull
        Sexe sexe,

        @DecimalMin(value = "0.0", inclusive = true)
        Float taille,

        @DecimalMin(value = "0.0", inclusive = true)
        Float poids,

        @NotNull
        Statut statut,

        // optionnel
        Role role,

        Espece espece,

        @Size(max = 100)
        String nom,

        @Valid
        @Size(max = 50)
        List<VaccinCreateRequestDto> vaccins,

        @Valid
        DeviceCreateRequestDto device

) {}

