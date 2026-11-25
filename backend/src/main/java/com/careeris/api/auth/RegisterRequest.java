package com.careeris.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Registration form data.
 * BUG-05 fix (Liyanarachchi L.A.E.E, EG/2023/5708): the e-mail must have a domain with a dot and a
 * top-level domain (e.g. example.com). Plain @Email accepted addresses such as "nimal@localhost".
 */
public record RegisterRequest(
        @NotNull UserRole role,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 120) String contactName,
        @NotBlank
        @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$",
                message = "must be a valid e-mail address with a domain, such as name@example.com")
        @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 72) String password) {
}
