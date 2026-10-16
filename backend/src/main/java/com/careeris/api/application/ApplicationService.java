package com.careeris.api.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import com.careeris.api.auth.UserAccount;
import com.careeris.api.auth.UserAccountRepository;
import com.careeris.api.vacancy.Vacancy;
import com.careeris.api.vacancy.VacancyRepository;
import com.careeris.api.vacancy.VacancyStatus;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ApplicationService {
    private final InternshipApplicationRepository applications;
    private final VacancyRepository vacancies;
    private final UserAccountRepository users;

    public ApplicationService(InternshipApplicationRepository applications, VacancyRepository vacancies, UserAccountRepository users) {
        this.applications = applications;
        this.vacancies = vacancies;
        this.users = users;
    }

    public List<ApplicationResponse> listForStudent(String studentId) {
        return applications.findByStudentIdOrderByAppliedAtDesc(studentId).stream().map(ApplicationResponse::from).toList();
    }

    public List<ApplicationResponse> listForCompany(String companyId, String vacancyId) {
        List<InternshipApplication> results = vacancyId == null || vacancyId.isBlank()
                ? applications.findByCompanyIdOrderByAppliedAtDesc(companyId)
                : applications.findByCompanyIdAndVacancyIdOrderByAppliedAtDesc(companyId, vacancyId);
        return results.stream().map(ApplicationResponse::from).toList();
    }

    public ApplicationResponse updateStatus(String companyId, String applicationId, ApplicationStatus status) {
        if (status != ApplicationStatus.UNDER_REVIEW && status != ApplicationStatus.INTERVIEW && status != ApplicationStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose Under review, Interview, or Rejected.");
        }
        InternshipApplication application = applications.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found."));
        if (!application.getCompanyId().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot update another company's application.");
        }
        application.setStatus(status);
        return ApplicationResponse.from(applications.save(application));
    }

    public ApplicationResponse scheduleInterview(String companyId, String applicationId, InterviewScheduleRequest request) {
        InternshipApplication application = applications.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found."));
        if (!application.getCompanyId().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot update another company's application.");
        }
        if (application.getStatus() != ApplicationStatus.INTERVIEW) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invite the student to interview before scheduling a time.");
        }
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Colombo"));
        if (request.date().isBefore(today) || (request.date().equals(today) && request.time().isBefore(LocalTime.now(ZoneId.of("Asia/Colombo"))))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an interview time in the future.");
        }
        application.setInterview(request.date(), request.time(), request.location(), request.notes());
        return ApplicationResponse.from(applications.save(application));
    }

    public ApplicationResponse apply(String studentId, ApplicationRequest request) {
        if (applications.findByStudentIdAndVacancyId(studentId, request.vacancyId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already applied to this vacancy.");
        }
        UserAccount student = users.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Student account no longer exists."));
        Vacancy vacancy = vacancies.findByIdAndStatus(request.vacancyId(), VacancyStatus.OPEN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This vacancy is no longer open."));
        if (vacancy.getDeadline().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The application deadline has passed.");
        }

        InternshipApplication application = new InternshipApplication(studentId, student.getDisplayName(), student.getEmail(),
                vacancy.getCompanyId(), vacancy.getCompanyName(), vacancy.getId(), vacancy.getTitle(), request.coverLetter());
        try {
            return ApplicationResponse.from(applications.save(application));
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already applied to this vacancy.");
        }
    }
}
