package com.cic.inventory.dtos.auth;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record AuthUserResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String location,
        String department,
        String role,
        List<String> roles,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
