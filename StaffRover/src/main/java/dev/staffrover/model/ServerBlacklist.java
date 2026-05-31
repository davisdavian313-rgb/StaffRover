package dev.staffrover.model;

import java.time.Instant;
import java.util.UUID;

public record ServerBlacklist(
        int id,
        UUID target,
        String targetName,
        String serverName,
        UUID staff,
        String staffName,
        String reason,
        boolean active,
        Instant createdAt
) {
}
