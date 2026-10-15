package com.careeris.api.vacancy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record VacancyResponse(
        String id,
        String companyId,
        String companyName,
        String title,
        String description,
        List<String> skills,
        String location,
        VacancyWorkMode workMode,
        LocalDate deadline,
        VacancyStatus status,
        Instant createdAt,
        Instant updatedAt) {
    public static VacancyResponse from(Vacancy vacancy) {
        return new VacancyResponse(vacancy.getId(), vacancy.getCompanyId(), vacancy.getCompanyName(), vacancy.getTitle(),
                vacancy.getDescription(), vacancy.getSkills(), vacancy.getLocation(), vacancy.getWorkMode(),
                vacancy.getDeadline(), vacancy.getStatus(), vacancy.getCreatedAt(), vacancy.getUpdatedAt());
    }
}
