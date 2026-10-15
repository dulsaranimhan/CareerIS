package com.careeris.api.auth;

import java.time.Instant;

public record RegisterResponse(String id, String email, String name, UserRole role, Instant createdAt) {
    public static RegisterResponse from(UserAccount account) {
        return new RegisterResponse(
                account.getId(),
                account.getEmail(),
                account.getDisplayName(),
                account.getRole(),
                account.getCreatedAt());
    }
}
