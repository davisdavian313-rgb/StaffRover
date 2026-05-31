package dev.staffrover.model;

import java.time.Instant;
import java.util.UUID;

public record Report(
        int id,
        UUID reporter,
        String reporterName,
        UUID target,
        String targetName,
        String serverName,
        String reason,
        String status,
        UUID claimedBy,
        Instant createdAt
) {
}
