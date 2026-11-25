package com.careeris.api.blackbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import com.careeris.api.application.ApplicationResponse;
import com.careeris.api.application.ApplicationService;
import com.careeris.api.application.ApplicationStatus;
import com.careeris.api.application.InternshipApplication;
import com.careeris.api.application.InternshipApplicationRepository;
import com.careeris.api.application.InterviewScheduleRequest;
import com.careeris.api.auth.UserAccountRepository;
import com.careeris.api.vacancy.VacancyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.web.server.ResponseStatusException;

/**
 * EC4206 Section 3.3 - Decision Table for "Schedule interview".
 *
 * Conditions                         R1  R2  R3  R4  R5  R6
 *  C1 Application exists?             N   Y   Y   Y   Y   Y
 *  C2 Owned by this company?          -   N   Y   Y   Y   Y
 *  C3 Status = INTERVIEW?             -   -   N   Y   Y   Y
 *  C4 Date/time in the future?        -   -   -   N   N*  Y
 * Actions
 *  A  HTTP result                    404 403 409 400 400 200
 *  (* R5 = today with a time that already passed)
 *
 * Owner: EG/2023/5664 - Jayasuriya J.A.D.N
 */
@DisplayName("Black-box: Decision table - schedule interview")
class InterviewSchedulingDecisionTableTest {
    private static final String COMPANY = "company-A";
    private static final ZoneId COLOMBO = ZoneId.of("Asia/Colombo");

    @ParameterizedTest(name = "{0}: exists={1}, owned={2}, interviewStatus={3}, when={4} -> HTTP {5}")
    @CsvSource({
            "R1, false, false, false, FUTURE_DATE, 404",
            "R2, true,  false, true,  FUTURE_DATE, 403",
            "R3, true,  true,  false, FUTURE_DATE, 409",
            "R4, true,  true,  true,  PAST_DATE,   400",
            "R5, true,  true,  true,  TODAY_PAST,  400",
            "R6, true,  true,  true,  FUTURE_DATE, 200"
    })
    void decisionTable(String rule, boolean exists, boolean owned, boolean interviewStatus, String when, int expectedHttp) {
        InternshipApplicationRepository applications = mock(InternshipApplicationRepository.class);
        ApplicationService service = new ApplicationService(applications, mock(VacancyRepository.class), mock(UserAccountRepository.class));

        InternshipApplication application = new InternshipApplication("student-1", "Nimal", "nimal@example.com",
                owned ? COMPANY : "company-B", "Acme", "vacancy-1", "Intern", "");
        if (interviewStatus) {
            application.setStatus(ApplicationStatus.INTERVIEW);
        }
        when(applications.findById("app-1")).thenReturn(exists ? Optional.of(application) : Optional.empty());
        lenient().when(applications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate today = LocalDate.now(COLOMBO);
        InterviewScheduleRequest request = switch (when) {
            case "PAST_DATE" -> new InterviewScheduleRequest(today.minusDays(1), LocalTime.NOON, "Zoom", null);
            case "TODAY_PAST" -> new InterviewScheduleRequest(today, LocalTime.MIN, "Zoom", null);
            default -> new InterviewScheduleRequest(today.plusDays(2), LocalTime.of(10, 0), "Zoom", null);
        };

        if (expectedHttp == 200) {
            ApplicationResponse response = service.scheduleInterview(COMPANY, "app-1", request);
            assertThat(response.interviewDate()).isEqualTo(request.date());
            assertThat(response.interviewLocation()).isEqualTo("Zoom");
        } else {
            assertThatThrownBy(() -> service.scheduleInterview(COMPANY, "app-1", request))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(expectedHttp));
        }
    }
}
