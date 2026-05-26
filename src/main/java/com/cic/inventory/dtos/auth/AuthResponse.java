package com.cic.inventory.dtos.auth;

import lombok.Builder;

import java.time.Instant;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        AuthUserResponse user
) {
}
