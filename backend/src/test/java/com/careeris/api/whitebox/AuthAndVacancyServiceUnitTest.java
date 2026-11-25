package com.careeris.api.whitebox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.careeris.api.auth.AuthService;
import com.careeris.api.auth.EmailAlreadyRegisteredException;
import com.careeris.api.auth.InvalidCredentialsException;
import com.careeris.api.auth.LoginRequest;
import com.careeris.api.auth.RegisterRequest;
import com.careeris.api.auth.UserAccount;
import com.careeris.api.auth.UserAccountRepository;
import com.careeris.api.auth.UserRole;
import com.careeris.api.vacancy.Vacancy;
import com.careeris.api.vacancy.VacancyRepository;
import com.careeris.api.vacancy.VacancyRequest;
import com.careeris.api.vacancy.VacancyResponse;
import com.careeris.api.vacancy.VacancyService;
import com.careeris.api.vacancy.VacancyStatus;
import com.careeris.api.vacancy.VacancyWorkMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * EC4206 Section 3.4 / 3.5 - JUnit 5 + Mockito unit tests for AuthService and VacancyService
 * (statement + branch coverage of every decision in both services).
 *
 * Owner: EG/2023/5941 - Wanasinghe W.M.C.O.B
 */
@DisplayName("Unit: AuthService & VacancyService")
class AuthAndVacancyServiceUnitTest {
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4); // low cost = fast tests

    @Nested
    @DisplayName("AuthService.register / authenticate")
    class Auth {
        private final UserAccountRepository users = mock(UserAccountRepository.class);
        private final AuthService service = new AuthService(users, encoder);

        @Test
        @DisplayName("UT-AU-01: e-mail is trimmed and lower-cased, password is hashed")
        void registerNormalisesEmailAndHashesPassword() {
            when(users.findByEmailIgnoreCase("nimal@example.com")).thenReturn(Optional.empty());
            when(users.save(any())).thenAnswer(i -> i.getArgument(0));
            UserAccount saved = service.register(new RegisterRequest(UserRole.STUDENT, "  Nimal  ", null, "  Nimal@Example.COM ", "Password123"));
            assertThat(saved.getEmail()).isEqualTo("nimal@example.com");
            assertThat(saved.getDisplayName()).isEqualTo("Nimal");
            assertThat(encoder.matches("Password123", saved.getPasswordHash())).isTrue();
        }

        @Test
        @DisplayName("UT-AU-02: company without contact name -> IllegalArgumentException")
        void companyNeedsContactName() {
            assertThatThrownBy(() -> service.register(new RegisterRequest(UserRole.COMPANY, "Acme", "  ", "hr@acme.com", "Password123")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("UT-AU-03: company with contact name is trimmed and saved")
        void companyWithContactName() {
            when(users.findByEmailIgnoreCase("hr@acme.com")).thenReturn(Optional.empty());
            when(users.save(any())).thenAnswer(i -> i.getArgument(0));
            UserAccount saved = service.register(new RegisterRequest(UserRole.COMPANY, "Acme", " Kamal ", "hr@acme.com", "Password123"));
            assertThat(saved.getContactName()).isEqualTo("Kamal");
            assertThat(saved.getRole()).isEqualTo(UserRole.COMPANY);
        }

        @Test
        @DisplayName("UT-AU-04: duplicate e-mail found by lookup -> EmailAlreadyRegisteredException")
        void duplicateEmailLookup() {
            when(users.findByEmailIgnoreCase("nimal@example.com"))
                    .thenReturn(Optional.of(new UserAccount("nimal@example.com", "N", null, "h", UserRole.STUDENT)));
            assertThatThrownBy(() -> service.register(new RegisterRequest(UserRole.STUDENT, "Nimal", null, "nimal@example.com", "Password123")))
                    .isInstanceOf(EmailAlreadyRegisteredException.class);
        }

        @Test
        @DisplayName("UT-AU-05: duplicate e-mail caught by unique index (race) -> EmailAlreadyRegisteredException")
        void duplicateEmailRace() {
            when(users.findByEmailIgnoreCase("nimal@example.com")).thenReturn(Optional.empty());
            when(users.save(any())).thenThrow(new DuplicateKeyException("dup"));
            assertThatThrownBy(() -> service.register(new RegisterRequest(UserRole.STUDENT, "Nimal", null, "nimal@example.com", "Password123")))
                    .isInstanceOf(EmailAlreadyRegisteredException.class);
        }

        @Test
        @DisplayName("UT-AU-06..08: authenticate - correct, wrong password, wrong role")
        void authenticateBranches() {
            UserAccount account = new UserAccount("nimal@example.com", "Nimal", null, encoder.encode("Password123"), UserRole.STUDENT);
            when(users.findByEmailIgnoreCase("nimal@example.com")).thenReturn(Optional.of(account));

            assertThat(service.authenticate(new LoginRequest(UserRole.STUDENT, " NIMAL@example.com", "Password123"))).isSameAs(account);
            assertThatThrownBy(() -> service.authenticate(new LoginRequest(UserRole.STUDENT, "nimal@example.com", "WrongPass1")))
                    .isInstanceOf(InvalidCredentialsException.class);
            assertThatThrownBy(() -> service.authenticate(new LoginRequest(UserRole.COMPANY, "nimal@example.com", "Password123")))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("VacancyService")
    class Vacancies {
        private final VacancyRepository vacancies = mock(VacancyRepository.class);
        private final UserAccountRepository users = mock(UserAccountRepository.class);
        private final VacancyService service = new VacancyService(vacancies, users);

        private Vacancy vacancy(String companyId, LocalDate deadline) {
            Vacancy v = new Vacancy(companyId, "Acme", new VacancyRequest(" Java Intern ", " desc ",
                    List.of(" Java ", "Java", "  ", "Spring"), " Colombo ", VacancyWorkMode.HYBRID, deadline));
            ReflectionTestUtils.setField(v, "id", "vac-" + deadline);
            return v;
        }

        @Test
        @DisplayName("UT-VA-01: listOpen hides vacancies whose deadline has passed")
        void listOpenFiltersExpired() {
            when(vacancies.findByStatusOrderByCreatedAtDesc(VacancyStatus.OPEN)).thenReturn(List.of(
                    vacancy("c1", LocalDate.now().minusDays(1)), vacancy("c1", LocalDate.now()), vacancy("c1", LocalDate.now().plusDays(9))));
            assertThat(service.listOpen()).hasSize(2);
        }

        @Test
        @DisplayName("UT-VA-02: skills are trimmed, blanks removed, duplicates removed")
        void skillsAreCleaned() {
            assertThat(vacancy("c1", LocalDate.now()).getSkills()).containsExactly("Java", "Spring");
        }

        @Test
        @DisplayName("UT-VA-03: getOpen - missing -> 404, expired -> 404, valid -> returned")
        void getOpenBranches() {
            when(vacancies.findByIdAndStatus("missing", VacancyStatus.OPEN)).thenReturn(Optional.empty());
            when(vacancies.findByIdAndStatus("old", VacancyStatus.OPEN)).thenReturn(Optional.of(vacancy("c1", LocalDate.now().minusDays(2))));
            when(vacancies.findByIdAndStatus("ok", VacancyStatus.OPEN)).thenReturn(Optional.of(vacancy("c1", LocalDate.now().plusDays(2))));
            assertThatThrownBy(() -> service.getOpen("missing")).isInstanceOf(ResponseStatusException.class);
            assertThatThrownBy(() -> service.getOpen("old")).isInstanceOf(ResponseStatusException.class);
            assertThat(service.getOpen("ok").title()).isEqualTo("Java Intern");
        }

        @Test
        @DisplayName("UT-VA-04: create - company deleted -> 401; otherwise saved with company name")
        void createBranches() {
            VacancyRequest request = new VacancyRequest("Intern", "d", List.of("Java"), "Kandy", VacancyWorkMode.REMOTE, LocalDate.now());
            when(users.findById("gone")).thenReturn(Optional.empty());
            when(users.findById("c1")).thenReturn(Optional.of(new UserAccount("hr@acme.com", "Acme Ltd", "K", "h", UserRole.COMPANY)));
            when(vacancies.save(any())).thenAnswer(i -> i.getArgument(0));
            assertThatThrownBy(() -> service.create("gone", request)).isInstanceOf(ResponseStatusException.class);
            VacancyResponse created = service.create("c1", request);
            assertThat(created.companyName()).isEqualTo("Acme Ltd");
            assertThat(created.status()).isEqualTo(VacancyStatus.OPEN);
        }

        @Test
        @DisplayName("UT-VA-05: update/updateStatus - not found 404, not owner 403, owner OK")
        void ownershipBranches() {
            Vacancy owned = vacancy("c1", LocalDate.now().plusDays(1));
            when(vacancies.findById("none")).thenReturn(Optional.empty());
            when(vacancies.findById("v1")).thenReturn(Optional.of(owned));
            when(vacancies.save(any())).thenAnswer(i -> i.getArgument(0));
            assertThatThrownBy(() -> service.updateStatus("c1", "none", VacancyStatus.CLOSED))
                    .satisfies(t -> assertThat(((ResponseStatusException) t).getStatusCode().value()).isEqualTo(404));
            assertThatThrownBy(() -> service.updateStatus("c2", "v1", VacancyStatus.CLOSED))
                    .satisfies(t -> assertThat(((ResponseStatusException) t).getStatusCode().value()).isEqualTo(403));
            assertThat(service.updateStatus("c1", "v1", VacancyStatus.CLOSED).status()).isEqualTo(VacancyStatus.CLOSED);
            assertThat(service.update("c1", "v1", new VacancyRequest("New title", "d", List.of("Go"), "Galle",
                    VacancyWorkMode.ON_SITE, LocalDate.now().plusDays(3))).title()).isEqualTo("New title");
            assertThat(service.listForCompany("c1")).isEmpty();
        }
    }
}
