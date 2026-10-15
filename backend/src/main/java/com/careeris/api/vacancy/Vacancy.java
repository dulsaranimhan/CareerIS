package com.careeris.api.vacancy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vacancies")
public class Vacancy {
    @Id
    private String id;

    @Indexed
    private String companyId;
    private String companyName;
    private String title;
    private String description;
    private List<String> skills;
    private String location;
    private VacancyWorkMode workMode;
    private LocalDate deadline;
    private VacancyStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    protected Vacancy() {
    }

    public Vacancy(String companyId, String companyName, VacancyRequest request) {
        this.companyId = companyId;
        this.companyName = companyName;
        updateFrom(request);
        this.status = VacancyStatus.OPEN;
        this.createdAt = Instant.now();
    }

    public void updateFrom(VacancyRequest request) {
        this.title = request.title().trim();
        this.description = request.description().trim();
        this.skills = request.skills().stream().map(String::trim).filter(skill -> !skill.isBlank()).distinct().toList();
        this.location = request.location().trim();
        this.workMode = request.workMode();
        this.deadline = request.deadline();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getCompanyId() { return companyId; }
    public String getCompanyName() { return companyName; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getSkills() { return skills; }
    public String getLocation() { return location; }
    public VacancyWorkMode getWorkMode() { return workMode; }
    public LocalDate getDeadline() { return deadline; }
    public VacancyStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setStatus(VacancyStatus status) { this.status = status; this.updatedAt = Instant.now(); }
}
