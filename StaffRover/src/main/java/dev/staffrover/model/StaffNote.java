package dev.staffrover.model;

import java.time.Instant;
import java.util.UUID;

public record StaffNote(
        int id,
        UUID target,
        String targetName,
        UUID staff,
        String staffName,
        String note,
        Instant createdAt
) {
}
