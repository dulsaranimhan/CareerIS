package com.careeris.api.application;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Allowed application status changes (state machine).
 * Written test-first (TDD) to fix BUG-02: a REJECTED application could be moved back to INTERVIEW.
 */
public final class ApplicationStatusTransitions {
    private static final Map<ApplicationStatus, Set<ApplicationStatus>> NEXT = Map.of(
            ApplicationStatus.SUBMITTED, EnumSet.of(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED),
            ApplicationStatus.UNDER_REVIEW, EnumSet.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED),
            ApplicationStatus.INTERVIEW, EnumSet.of(ApplicationStatus.REJECTED),
            ApplicationStatus.REJECTED, EnumSet.noneOf(ApplicationStatus.class),
            ApplicationStatus.WITHDRAWN, EnumSet.noneOf(ApplicationStatus.class));

    private ApplicationStatusTransitions() { }

    public static boolean isAllowed(ApplicationStatus from, ApplicationStatus to) {
        if (from == null || to == null) {
            return false;
        }
        if (from == to) {
            return from != ApplicationStatus.WITHDRAWN; // re-saving the same status is harmless
        }
        return NEXT.get(from).contains(to);
    }
}
