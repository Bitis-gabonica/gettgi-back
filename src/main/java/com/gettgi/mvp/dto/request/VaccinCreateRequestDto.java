package com.gettgi.mvp.dto.request;

import com.gettgi.mvp.entity.enums.TypeVaccin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

public record VaccinCreateRequestDto(

        @NotNull
        TypeVaccin typeVaccin,

        @NotNull
        @PastOrPresent(message = "La date de vaccination ne doit pas être dans le futur")
        LocalDate date
) {}