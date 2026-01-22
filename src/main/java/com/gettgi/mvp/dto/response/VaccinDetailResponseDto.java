package com.gettgi.mvp.dto.response;

import com.gettgi.mvp.entity.enums.TypeVaccin;

import java.time.LocalDate;

public record VaccinDetailResponseDto(
        TypeVaccin typeVaccin,
        LocalDate date
) {}

