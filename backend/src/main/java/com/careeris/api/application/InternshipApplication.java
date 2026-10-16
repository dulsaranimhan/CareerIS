package com.careeris.api.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "applications")
@CompoundIndexes({
        @CompoundIndex(name = "student_vacancy_unique", def = "{'studentId': 1, 'vacancyId': 1}", unique = true),
        @CompoundIndex(name = "company_applications", def = "{'companyId': 1, 'appliedAt': -1}")
})
public class InternshipApplication {
    @Id
    private String id;

    @Indexed
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String companyId;
    private String companyName;
    private String vacancyId;
    private String vacancyTitle;
    private String coverLetter;
    private ApplicationStatus status;
    private Instant appliedAt;
    private Instant updatedAt;
    private LocalDate interviewDate;
    private LocalTime interviewTime;
    private String interviewLocation;
    private String interviewNotes;

    protected InternshipApplication() {
    }

    public InternshipApplication(String studentId, String studentName, String studentEmail,
            String companyId, String companyName, String vacancyId, String vacancyTitle, String coverLetter) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.companyId = companyId;
        this.companyName = companyName;
        this.vacancyId = vacancyId;
        this.vacancyTitle = vacancyTitle;
        this.coverLetter = coverLetter == null ? "" : coverLetter.trim();
        this.status = ApplicationStatus.SUBMITTED;
        this.appliedAt = Instant.now();
        this.updatedAt = this.appliedAt;
    }

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public String getCompanyId() { return companyId; }
    public String getCompanyName() { return companyName; }
    public String getVacancyId() { return vacancyId; }
    public String getVacancyTitle() { return vacancyTitle; }
    public String getCoverLetter() { return coverLetter; }
    public ApplicationStatus getStatus() { return status; }
    public Instant getAppliedAt() { return appliedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public LocalDate getInterviewDate() { return interviewDate; }
    public LocalTime getInterviewTime() { return interviewTime; }
    public String getInterviewLocation() { return interviewLocation; }
    public String getInterviewNotes() { return interviewNotes; }
    public void setStatus(ApplicationStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
    public void setInterview(LocalDate date, LocalTime time, String location, String notes) {
        this.interviewDate = date;
        this.interviewTime = time;
        this.interviewLocation = location.trim();
        this.interviewNotes = notes == null || notes.isBlank() ? null : notes.trim();
        this.updatedAt = Instant.now();
    }
}
