package com.careeris.api.application;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InterviewScheduleRequest(
        @NotNull LocalDate date,
        @NotNull LocalTime time,
        @NotBlank @Size(max = 240) String location,
        @Size(max = 1000) String notes) {
}
