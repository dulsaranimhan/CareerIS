package com.careeris.api.profile;

import java.util.List;

import jakarta.validation.constraints.Size;

public record StudentProfileRequest(
        @Size(max = 160) String education,
        @Size(max = 240) String qualification,
        @Size(max = 1500) String profileSummary,
        @Size(max = 30) List<@Size(max = 60) String> skills) {
}
