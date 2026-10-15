package com.careeris.api.application;

import java.time.Instant;

public record ApplicationResponse(
        String id,
        String studentId,
        String studentName,
        String studentEmail,
        String companyId,
        String companyName,
        String vacancyId,
        String vacancyTitle,
        String coverLetter,
        ApplicationStatus status,
        Instant appliedAt,
        Instant updatedAt) {
    public static ApplicationResponse from(InternshipApplication application) {
        return new ApplicationResponse(application.getId(), application.getStudentId(), application.getStudentName(),
                application.getStudentEmail(), application.getCompanyId(), application.getCompanyName(),
                application.getVacancyId(), application.getVacancyTitle(), application.getCoverLetter(),
                application.getStatus(), application.getAppliedAt(), application.getUpdatedAt());
    }
}
