package com.careeris.api.vacancy;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VacancyRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 5000) String description,
        @NotEmpty @Size(max = 12) List<@NotBlank @Size(max = 40) String> skills,
        @NotBlank @Size(max = 120) String location,
        @NotNull VacancyWorkMode workMode,
        @NotNull @FutureOrPresent LocalDate deadline) {
}
