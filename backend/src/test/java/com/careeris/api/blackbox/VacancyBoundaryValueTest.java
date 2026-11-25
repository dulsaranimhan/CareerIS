package com.careeris.api.blackbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import com.careeris.api.application.ApplicationRequest;
import com.careeris.api.application.InterviewScheduleRequest;
import com.careeris.api.vacancy.VacancyRequest;
import com.careeris.api.vacancy.VacancyWorkMode;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * EC4206 Section 3.3 - Boundary Value Analysis on numeric / date fields.
 *   - Vacancy deadline (date)          : must be today or later
 *   - Vacancy skills (count)           : 1..12 skills, each 1..40 chars
 *   - Vacancy title (length)           : 1..120
 *   - Cover letter (length)            : 0..3000
 *   - Interview location / notes       : 1..240 / 0..1000
 *
 * Owner: EG/2023/5664 - Jayasuriya J.A.D.N
 */
@DisplayName("Black-box: Vacancy & application BVA")
class VacancyBoundaryValueTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static VacancyRequest vacancy(String title, List<String> skills, LocalDate deadline) {
        return new VacancyRequest(title, "Real project work", skills, "Colombo", VacancyWorkMode.REMOTE, deadline);
    }

    private static boolean fieldValid(Object request, String field) {
        return validator.validate(request).stream().noneMatch(v -> v.getPropertyPath().toString().startsWith(field));
    }

    // ---------- Date BVA: deadline ----------
    @ParameterizedTest(name = "TC-BB-DL-{index}: deadline today{0} days -> valid={1}")
    @CsvSource({"-365, false", "-1, false", "0, true", "1, true", "365, true"})
    void deadlineBoundaries(int offsetDays, boolean expectedValid) {
        VacancyRequest request = vacancy("Intern", List.of("Java"), LocalDate.now().plusDays(offsetDays));
        assertThat(fieldValid(request, "deadline")).isEqualTo(expectedValid);
    }

    @Test
    @DisplayName("TC-BB-DL-OBS: deadline in year 9999 is accepted (no upper bound) - see BUG-06")
    void deadlineHasNoUpperBound() {
        VacancyRequest request = vacancy("Intern", List.of("Java"), LocalDate.of(9999, 12, 31));
        assertThat(fieldValid(request, "deadline")).isTrue();
    }

    // ---------- Numeric BVA: number of skills (1..12) ----------
    @ParameterizedTest(name = "TC-BB-SK-{index}: {0} skills -> valid={1}")
    @CsvSource({"0, false", "1, true", "2, true", "11, true", "12, true", "13, false"})
    void skillCountBoundaries(int count, boolean expectedValid) {
        List<String> skills = IntStream.rangeClosed(1, count).mapToObj(i -> "Skill" + i).toList();
        assertThat(fieldValid(vacancy("Intern", skills, LocalDate.now().plusDays(5)), "skills")).isEqualTo(expectedValid);
    }

    // ---------- Length BVA: each skill (1..40) ----------
    @ParameterizedTest(name = "TC-BB-SL-{index}: skill length {0} -> valid={1}")
    @CsvSource({"39, true", "40, true", "41, false"})
    void skillLengthBoundaries(int length, boolean expectedValid) {
        List<String> skills = Collections.singletonList("s".repeat(length));
        assertThat(fieldValid(vacancy("Intern", skills, LocalDate.now().plusDays(5)), "skills")).isEqualTo(expectedValid);
    }

    // ---------- Length BVA: title (1..120) ----------
    @ParameterizedTest(name = "TC-BB-TT-{index}: title length {0} -> valid={1}")
    @CsvSource({"0, false", "1, true", "120, true", "121, false"})
    void titleBoundaries(int length, boolean expectedValid) {
        assertThat(fieldValid(vacancy("T".repeat(length), List.of("Java"), LocalDate.now().plusDays(5)), "title"))
                .isEqualTo(expectedValid);
    }

    // ---------- Length BVA: cover letter (0..3000) ----------
    @ParameterizedTest(name = "TC-BB-CL-{index}: cover letter length {0} -> valid={1}")
    @CsvSource({"0, true", "2999, true", "3000, true", "3001, false"})
    void coverLetterBoundaries(int length, boolean expectedValid) {
        assertThat(fieldValid(new ApplicationRequest("vacancy-1", "c".repeat(length)), "coverLetter")).isEqualTo(expectedValid);
    }

    // ---------- Length BVA: interview location (1..240) and notes (0..1000) ----------
    @ParameterizedTest(name = "TC-BB-IV-{index}: location {0} chars, notes {1} chars -> valid={2}")
    @CsvSource({
            "0,   0,    false",
            "1,   0,    true",
            "240, 1000, true",
            "241, 10,   false",
            "10,  1001, false"
    })
    void interviewFieldBoundaries(int locationLength, int notesLength, boolean expectedValid) {
        InterviewScheduleRequest request = new InterviewScheduleRequest(LocalDate.now().plusDays(3), LocalTime.NOON,
                "L".repeat(locationLength), "n".repeat(notesLength));
        assertThat(validator.validate(request).isEmpty()).isEqualTo(expectedValid);
    }
}
