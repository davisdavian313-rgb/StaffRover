package dev.staffrover.model;

import java.time.Instant;
import java.util.UUID;

public record Evidence(
        int id,
        UUID target,
        String targetName,
        UUID staff,
        String staffName,
        String url,
        String note,
        Integer reportId,
        Integer punishmentId,
        Instant createdAt
) {
}
