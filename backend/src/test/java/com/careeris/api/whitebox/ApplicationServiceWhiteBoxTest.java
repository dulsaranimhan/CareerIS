package com.careeris.api.whitebox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import com.careeris.api.application.ApplicationRequest;
import com.careeris.api.application.ApplicationResponse;
import com.careeris.api.application.ApplicationService;
import com.careeris.api.application.ApplicationStatus;
import com.careeris.api.application.InternshipApplication;
import com.careeris.api.application.InternshipApplicationRepository;
import com.careeris.api.application.InterviewScheduleRequest;
import com.careeris.api.auth.UserAccount;
import com.careeris.api.auth.UserAccountRepository;
import com.careeris.api.auth.UserRole;
import com.careeris.api.vacancy.Vacancy;
import com.careeris.api.vacancy.VacancyRepository;
import com.careeris.api.vacancy.VacancyRequest;
import com.careeris.api.vacancy.VacancyStatus;
import com.careeris.api.vacancy.VacancyWorkMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * EC4206 Section 3.4 - White-box testing of ApplicationService.
 * Test cases are derived from the control-flow graph (CFG) of each method so that
 * every statement and every branch (true and false outcome of each decision) is executed.
 *
 * scheduleInterview(): decisions D1 found?  D2 owned?  D3 status==INTERVIEW?
 *                      D4 (date < today) || (date == today && time < now)   -> V(G) = 7 (atomic conditions)
 * apply():             decisions D1 already applied?  D2 student found?  D3 vacancy open?
 *                      D4 deadline passed?  D5 DuplicateKeyException?        -> V(G) = 6
 *
 * Owner: EG/2023/5941 - Wanasinghe W.M.C.O.B
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("White-box: ApplicationService branch coverage")
class ApplicationServiceWhiteBoxTest {
    private static final ZoneId COLOMBO = ZoneId.of("Asia/Colombo");

    @Mock InternshipApplicationRepository applications;
    @Mock VacancyRepository vacancies;
    @Mock UserAccountRepository users;
    @InjectMocks ApplicationService service;

    private InternshipApplication application;

    @BeforeEach
    void setUp() {
        application = new InternshipApplication("stu-1", "Nimal", "nimal@example.com", "comp-1", "Acme",
                "vac-1", "Java Intern", "  Hello  ");
    }

    private static int status(Throwable t) {
        return ((ResponseStatusException) t).getStatusCode().value();
    }

    @Nested
    @DisplayName("scheduleInterview() - basis paths")
    class ScheduleInterview {
        @Test
        @DisplayName("WB-SI-P1: D1=false -> 404 (application not found)")
        void path1NotFound() {
            when(applications.findById("app-1")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.scheduleInterview("comp-1", "app-1",
                    new InterviewScheduleRequest(LocalDate.now(COLOMBO).plusDays(1), LocalTime.NOON, "Zoom", null)))
                    .satisfies(t -> assertThat(status(t)).isEqualTo(404));
        }

        @Test
        @DisplayName("WB-SI-P2: D1=true, D2=false -> 403 (other company)")
        void path2NotOwned() {
            when(applications.findById("app-1")).thenReturn(Optional.of(application));
            assertThatThrownBy(() -> service.scheduleInterview("comp-OTHER", "app-1",
                    new InterviewScheduleRequest(LocalDate.now(COLOMBO).plusDays(1), LocalTime.NOON, "Zoom", null)))
                    .satisfies(t -> assertThat(status(t)).isEqualTo(403));
        }

        @Test
        @DisplayName("WB-SI-P3: D3=false -> 409 (student not invited yet)")
        void path3NotInvited() {
            when(applications.findById("app-1")).thenReturn(Optional.of(application));
            assertThatThrownBy(() -> service.scheduleInterview("comp-1", "app-1",
                    new InterviewScheduleRequest(LocalDate.now(COLOMBO).plusDays(1), LocalTime.NOON, "Zoom", null)))
                    .satisfies(t -> assertThat(status(t)).isEqualTo(409));
        }

        @Test
        @DisplayName("WB-SI-P4: D4a=true (date before today) -> 400")
        void path4PastDate() {
            application.setStatus(ApplicationStatus.INTERVIEW);
            when(applications.findById("app-1")).thenReturn(Optional.of(application));
            assertThatThrownBy(() -> service.scheduleInterview("comp-1", "app-1",
                    new InterviewScheduleRequest(LocalDate.now(COLOMBO).minusDays(1), LocalTime.NOON, "Zoom", null)))
                    .satisfies(t -> assertThat(status(t)).isEqualTo(400));
        }

        @Test
        @DisplayName("WB-SI-P5: D4a=false, D4b=true, D4c=true (today, time already passed) -> 400")
        void path5TodayPastTime() {
            application.setStatus(ApplicationStatus.INTERVIEW);
            when(applications.findById("app-1")).thenReturn(Optional.of(application));
            assertThatThrownBy(() -> service.scheduleInterview("comp-1", "app-1",
                    new InterviewScheduleRequest(LocalDate.now(COLOMBO), LocalTime.MIN, "Zoom", null)))
                    .satisfies(t -> assertThat(status(t)).isEqualTo(400));
        }

        @Test
        @DisplayName("WB-SI-P6: D4b=true, D4c=false (today, later time) -> saved")
        void path6TodayFutureTime() {
            application.setStatus(ApplicationStatus.INTERVIEW);
            when(applications.findById("app-1")).thenReturn(Optional.of(application));
            when(applications.save(any())).thenAnswer(i -> i.getArgument(0));
            ApplicationResponse response = service.scheduleInterview("comp-1", "app-1",
                    new InterviewScheduleRequest(LocalDate.now(COLOMBO), LocalTime.MAX, "  Room 4  ", "   "));
            assertThat(response.interviewLocation()).isEqualTo("Room 4");   // trimmed
            assertThat(response.interviewNotes()).isNull();                 // blank notes -> null branch
        }

        @Test
        @DisplayName("WB-SI-P7: D4a=false, D4b=false (future date) -> saved with notes")
        void path7FutureDate() {
            application.setStatus(ApplicationStatus.INTERVIEW);
            when(applications.findById("app-1")).thenReturn(Optional.of(application));
            when(applications.save(any())).thenAnswer(i -> i.getArgument(0));
            ApplicationResponse response = service.scheduleInterview("comp-1", "app-1",
                    new InterviewScheduleRequest(LocalDate.now(COLOMBO).plusDays(7), LocalTime.of(9, 30), "https://meet.example/x", " Bring ID "));
            assertThat(response.interviewTime()).isEqualTo(LocalTime.of(9, 30));
            assertThat(response.interviewNotes()).isEqualTo("Bring ID");
        }
    }

    @Nested
    @DisplayName("apply() - basis paths")
    class Apply {
        private Vacancy openVacancy(LocalDate deadline) {
            Vacancy vacancy = new Vacancy("comp-1", "Acme", new VacancyRequest("Java Intern", "desc",
                    java.util.List.of("Java"), "Colombo", VacancyWorkMode.ON_SITE, deadline));
            ReflectionTestUtils.setField(vacancy, "id", "vac-1");
            return vacancy;
        }

        private final UserAccount student = new UserAccount("nimal@example.com", "Nimal", null, "hash", UserRole.STUDENT);

        @Test
        @DisplayName("WB-AP-P1: D1=true -> 409 already applied (no further calls)")
        void path1AlreadyApplied() {
            when(applications.findByStudentIdAndVacancyId("stu-1", "vac-1")).thenReturn(Optional.of(application));
            assertThatThrownBy(() -> service.apply("stu-1", new ApplicationRequest("vac-1", null)))
                    .satisfies(t -> assertThat(status(t)).isEqualTo(409));
            verify(users, never()).findById(any());
        }

        @Test
        @DisplayName("WB-AP-P2: D2=false -> 401 student account deleted")
        void path2StudentMissing() {
            when(applications.findByStudentIdAndVacancyId("stu-1", "vac-1")).thenReturn(Optional.empty());
            when(users.findById("stu-1")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.apply("stu-1", new ApplicationRequest("vac-1", null)))
                    .satisfies(t -> assertThat(status(t)).isEqualTo(401));
        }

        @Test
        @DisplayName("WB-AP-P3: D3=false -> 404 vacancy closed / missing")
        void path3VacancyNotOpen() {
            when(applications.findByStudentIdAndVacancyId("stu-1", "vac-1")).thenReturn(Optional.empty());
            when(users.findById("stu-1")).thenReturn(Optional.of(student));
            when(vacancies.findByIdAndStatus("vac-1", VacancyStatus.OPEN)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.apply("stu-1", new ApplicationRequest("vac-1", null)))
                    .satisfies(t -> assertThat(status(t)).isEqualTo(404));
        }

        @Test
        @DisplayName("WB-AP-P4: D4=true -> 409 deadline passed")
        void path4DeadlinePassed() {
            when(applications.findByStudentIdAndVacancyId("stu-1", "vac-1")).thenReturn(Optional.empty());
            when(users.findById("stu-1")).thenReturn(Optional.of(student));
            when(vacancies.findByIdAndStatus("vac-1", VacancyStatus.OPEN)).thenReturn(Optional.of(openVacancy(LocalDate.now().minusDays(1))));
            assertThatThrownBy(() -> service.apply("stu-1", new ApplicationRequest("vac-1", null)))
                    .satisfies(t -> assertThat(status(t)).isEqualTo(409));
        }

        @Test
        @DisplayName("WB-AP-P5: happy path -> SUBMITTED, null cover letter becomes empty string")
        void path5Success() {
            when(applications.findByStudentIdAndVacancyId("stu-1", "vac-1")).thenReturn(Optional.empty());
            when(users.findById("stu-1")).thenReturn(Optional.of(student));
            when(vacancies.findByIdAndStatus("vac-1", VacancyStatus.OPEN)).thenReturn(Optional.of(openVacancy(LocalDate.now())));
            when(applications.save(any())).thenAnswer(i -> i.getArgument(0));
            ApplicationResponse response = service.apply("stu-1", new ApplicationRequest("vac-1", null));
            assertThat(response.status()).isEqualTo(ApplicationStatus.SUBMITTED);
            assertThat(response.coverLetter()).isEmpty();
            assertThat(response.companyId()).isEqualTo("comp-1");
        }

        @Test
        @DisplayName("WB-AP-P6: D5=true -> race condition on unique index mapped to 409")
        void path6DuplicateKeyRace() {
            when(applications.findByStudentIdAndVacancyId("stu-1", "vac-1")).thenReturn(Optional.empty());
            when(users.findById("stu-1")).thenReturn(Optional.of(student));
            when(vacancies.findByIdAndStatus("vac-1", VacancyStatus.OPEN)).thenReturn(Optional.of(openVacancy(LocalDate.now().plusDays(3))));
            when(applications.save(any())).thenThrow(new DuplicateKeyException("dup"));
            assertThatThrownBy(() -> service.apply("stu-1", new ApplicationRequest("vac-1", "hi")))
                    .satisfies(t -> assertThat(status(t)).isEqualTo(409));
        }
    }

    @Nested
    @DisplayName("listForCompany() - both branches of the vacancy filter")
    class ListForCompany {
        @Test
        @DisplayName("WB-LC-01: vacancyId null -> all company applications")
        void noFilter() {
            when(applications.findByCompanyIdOrderByAppliedAtDesc("comp-1")).thenReturn(java.util.List.of(application));
            assertThat(service.listForCompany("comp-1", null)).hasSize(1);
        }

        @Test
        @DisplayName("WB-LC-02: vacancyId blank -> all company applications")
        void blankFilter() {
            when(applications.findByCompanyIdOrderByAppliedAtDesc("comp-1")).thenReturn(java.util.List.of());
            assertThat(service.listForCompany("comp-1", "  ")).isEmpty();
        }

        @Test
        @DisplayName("WB-LC-03: vacancyId given -> filtered query")
        void withFilter() {
            when(applications.findByCompanyIdAndVacancyIdOrderByAppliedAtDesc("comp-1", "vac-1")).thenReturn(java.util.List.of(application));
            assertThat(service.listForCompany("comp-1", "vac-1")).extracting(ApplicationResponse::vacancyId).containsExactly("vac-1");
        }
    }
}
