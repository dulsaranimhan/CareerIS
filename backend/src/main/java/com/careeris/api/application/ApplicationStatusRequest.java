package com.careeris.api.application;

import jakarta.validation.constraints.NotNull;

public record ApplicationStatusRequest(@NotNull ApplicationStatus status) {
}
