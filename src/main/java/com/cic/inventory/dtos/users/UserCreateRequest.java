package com.cic.inventory.dtos.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "First name is required")
        String firtName,
        @NotBlank(message = "Last name is required")
        String lastName,
        @Email(message = "A valid email is required")
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
        Long locationId,      // ✅ was: String location
        Long departmentId,
        @NotBlank(message = "Role is required")
        @Pattern(regexp = "^(admin|admin_user|user)$", message = "Invalid role")
        String role,
        Boolean isActive
) {
}
