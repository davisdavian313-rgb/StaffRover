package dev.staffrover.model;

import java.time.Instant;
import java.util.UUID;

public record WatchEntry(
        int id,
        UUID target,
        String targetName,
        UUID staff,
        String staffName,
        String reason,
        boolean active,
        Instant createdAt
) {
}
