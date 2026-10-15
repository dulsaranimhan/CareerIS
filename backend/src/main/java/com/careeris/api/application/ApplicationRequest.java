package com.careeris.api.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(
        @NotBlank String vacancyId,
        @Size(max = 3000) String coverLetter) {
}
