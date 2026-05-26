package com.cic.inventory.dtos.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        String firstName,
        String lastName,
        @Email(message = "A valid email is required")
        String email,
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
        String location,
        String department,
        @Pattern(regexp = "^(admin|admin_user|user)?$", message = "Invalid role")
        String role,
        Boolean isActive
) {
}
