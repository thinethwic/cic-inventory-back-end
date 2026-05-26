package com.cic.inventory.entities.types;

public enum UserRole {
    ADMIN,
    ADMIN_USER,
    USER;

    public String toClaimValue() {
        return name().toLowerCase();
    }

    public static UserRole fromValue(String value) {
        if (value == null || value.isBlank()) {
            return USER;
        }

        return UserRole.valueOf(value.trim().toUpperCase());
    }
}
