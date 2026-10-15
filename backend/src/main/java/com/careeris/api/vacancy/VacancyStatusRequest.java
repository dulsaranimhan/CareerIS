package com.careeris.api.vacancy;

import jakarta.validation.constraints.NotNull;

public record VacancyStatusRequest(@NotNull VacancyStatus status) {
}
