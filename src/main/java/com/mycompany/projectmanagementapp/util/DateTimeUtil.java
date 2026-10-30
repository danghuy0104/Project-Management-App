package com.mycompany.projectmanagementapp.util;

import com.mycompany.projectmanagementapp.exception.ApiException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

public final class DateTimeUtil {

    private DateTimeUtil() {
    }

    /**
     * Frontend sends "2025-10-31" (input type=date) or "2025-10-31T00:00:00.000Z" (toISOString).
     * Empty/null strings return null. Dates without a time are interpreted as 00:00 UTC.
     */
    public static Instant parse(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
            // try the next format
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
            // try the next format
        }
        try {
            return LocalDateTime.parse(text).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // try the next format
        }
        try {
            return LocalDate.parse(text).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (DateTimeParseException ignored) {
            throw ApiException.badRequest(
                    "Invalid date for '" + fieldName + "': " + text + " (expected yyyy-MM-dd or ISO-8601)");
        }
    }
}
