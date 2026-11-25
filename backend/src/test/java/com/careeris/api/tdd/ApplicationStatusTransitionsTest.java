package com.careeris.api.tdd;

import static org.assertj.core.api.Assertions.assertThat;

import com.careeris.api.application.ApplicationStatus;
import com.careeris.api.application.ApplicationStatusTransitions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * EC4206 Section 3.5 - Test-Driven Development (TDD).
 * Requirement written as tests FIRST (red), then ApplicationStatusTransitions was implemented (green)
 * and refactored into ApplicationService.updateStatus(). Fixes BUG-02 (rejected student re-invited).
 *
 * Rules (state machine):
 *   SUBMITTED    -> UNDER_REVIEW | INTERVIEW | REJECTED
 *   UNDER_REVIEW -> INTERVIEW | REJECTED
 *   INTERVIEW    -> REJECTED
 *   REJECTED, WITHDRAWN -> (final states, nothing)
 *   Saving the same status again is allowed (idempotent, used by the UI).
 *
 * Owner: EG/2023/5941 - Wanasinghe W.M.C.O.B
 */
@DisplayName("TDD: application status transitions")
class ApplicationStatusTransitionsTest {

    @ParameterizedTest(name = "TDD-{index}: {0} -> {1} allowed={2}")
    @CsvSource({
            "SUBMITTED,    UNDER_REVIEW, true",
            "SUBMITTED,    INTERVIEW,    true",
            "SUBMITTED,    REJECTED,     true",
            "UNDER_REVIEW, INTERVIEW,    true",
            "UNDER_REVIEW, REJECTED,     true",
            "INTERVIEW,    REJECTED,     true",
            "INTERVIEW,    INTERVIEW,    true",
            "UNDER_REVIEW, SUBMITTED,    false",
            "INTERVIEW,    UNDER_REVIEW, false",
            "REJECTED,     INTERVIEW,    false",
            "REJECTED,     UNDER_REVIEW, false",
            "WITHDRAWN,    INTERVIEW,    false",
            "WITHDRAWN,    REJECTED,     false"
    })
    void transitionRules(ApplicationStatus from, ApplicationStatus to, boolean allowed) {
        assertThat(ApplicationStatusTransitions.isAllowed(from, to)).isEqualTo(allowed);
    }

    @ParameterizedTest(name = "TDD-null-{index}: null input is never allowed")
    @CsvSource({"SUBMITTED", "REJECTED"})
    void nullIsNeverAllowed(ApplicationStatus status) {
        assertThat(ApplicationStatusTransitions.isAllowed(status, null)).isFalse();
        assertThat(ApplicationStatusTransitions.isAllowed(null, status)).isFalse();
    }
}
