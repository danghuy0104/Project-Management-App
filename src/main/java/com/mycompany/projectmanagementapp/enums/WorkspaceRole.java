package com.mycompany.projectmanagementapp.enums;

import com.mycompany.projectmanagementapp.exception.ApiException;

import java.util.Locale;

public enum WorkspaceRole {
    ADMIN,
    MEMBER;

    public static WorkspaceRole fromString(String value) {
        if (value == null || value.isBlank()) {
            return MEMBER;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        int colon = normalized.lastIndexOf(':');
        if (colon >= 0) {
            normalized = normalized.substring(colon + 1);
        }
        return switch (normalized) {
            case "ADMIN" -> ADMIN;
            case "MEMBER" -> MEMBER;
            default -> throw ApiException.badRequest("Invalid role '" + value + "' (expected ADMIN or MEMBER)");
        };
    }
}
