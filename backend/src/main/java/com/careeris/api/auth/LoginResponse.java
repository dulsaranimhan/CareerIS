package com.careeris.api.auth;

import java.time.Instant;

public record LoginResponse(String accessToken, String tokenType, Instant expiresAt, RegisterResponse user) {
}
