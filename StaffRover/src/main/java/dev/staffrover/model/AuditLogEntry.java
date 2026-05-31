package dev.staffrover.model;

import java.time.Instant;
import java.util.UUID;

public record AuditLogEntry(
        int id,
        UUID staff,
        String staffName,
        String action,
        String details,
        Instant createdAt
) {
}
