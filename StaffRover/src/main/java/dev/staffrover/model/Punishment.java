package dev.staffrover.model;

import java.time.Instant;
import java.util.UUID;

public record Punishment(
        int id,
        UUID target,
        String targetName,
        UUID staff,
        String staffName,
        String category,
        String offense,
        int tier,
        String type,
        String duration,
        String reason,
        Integer evidenceId,
        String status,
        String appealStatus,
        Instant createdAt
) {
}
