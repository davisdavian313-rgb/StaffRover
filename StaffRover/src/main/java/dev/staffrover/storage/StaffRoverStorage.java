package dev.staffrover.storage;

import dev.staffrover.model.Evidence;
import dev.staffrover.model.PlayerProfile;
import dev.staffrover.model.Punishment;
import dev.staffrover.model.Report;
import dev.staffrover.model.ServerBlacklist;
import dev.staffrover.model.StaffNote;
import dev.staffrover.model.WatchEntry;
import dev.staffrover.model.AuditLogEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public final class StaffRoverStorage {
    private final Path dataDirectory;
    private final Map<UUID, PlayerProfile> profiles = new HashMap<>();
    private final List<Punishment> punishments = new ArrayList<>();
    private final List<Evidence> evidence = new ArrayList<>();
    private final List<Report> reports = new ArrayList<>();
    private final List<StaffNote> notes = new ArrayList<>();
    private final List<WatchEntry> watches = new ArrayList<>();
    private final List<AuditLogEntry> auditLog = new ArrayList<>();
    private final List<ServerBlacklist> serverBlacklists = new ArrayList<>();
    private int nextPunishmentId = 1;
    private int nextEvidenceId = 1;
    private int nextReportId = 1;
    private int nextNoteId = 1;
    private int nextWatchId = 1;
    private int nextAuditId = 1;
    private int nextServerBlacklistId = 1;

    public StaffRoverStorage(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public void load() throws IOException {
        Files.createDirectories(dataDirectory);
        loadProfiles();
        loadPunishments();
        loadEvidence();
        loadReports();
        loadNotes();
        loadWatches();
        loadAuditLog();
        loadServerBlacklists();
    }

    public synchronized void saveAll() {
        try {
            saveProfiles();
            savePunishments();
            saveEvidence();
            saveReports();
            saveNotes();
            saveWatches();
            saveAuditLog();
            saveServerBlacklists();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save STAFF data", exception);
        }
    }

    public synchronized PlayerProfile profile(UUID uuid, String name) {
        PlayerProfile profile = profiles.computeIfAbsent(uuid, key -> new PlayerProfile(uuid, name));
        profile.lastName(name);
        return profile;
    }

    public synchronized Optional<PlayerProfile> profileByName(String name) {
        return profiles.values().stream()
                .filter(profile -> profile.lastName().equalsIgnoreCase(name))
                .findFirst();
    }

    public synchronized Collection<PlayerProfile> profiles() {
        return List.copyOf(profiles.values());
    }

    public synchronized Punishment addPunishment(UUID target, String targetName, UUID staff, String staffName,
                                                String category, String offense, int tier, String type,
                                                String duration, String reason, Integer evidenceId) {
        Punishment punishment = new Punishment(nextPunishmentId++, target, targetName, staff, staffName,
                category, offense, tier, type, duration, reason, evidenceId, "ACTIVE", "NONE", Instant.now());
        punishments.add(punishment);
        saveAll();
        return punishment;
    }

    public synchronized Optional<Punishment> punishmentById(int id) {
        return punishments.stream().filter(punishment -> punishment.id() == id).findFirst();
    }

    public synchronized Optional<Punishment> revokePunishment(int id) {
        for (int index = 0; index < punishments.size(); index++) {
            Punishment punishment = punishments.get(index);
            if (punishment.id() == id) {
                Punishment updated = new Punishment(punishment.id(), punishment.target(), punishment.targetName(),
                        punishment.staff(), punishment.staffName(), punishment.category(), punishment.offense(),
                        punishment.tier(), punishment.type(), punishment.duration(), punishment.reason(),
                        punishment.evidenceId(), "REVOKED", punishment.appealStatus(), punishment.createdAt());
                punishments.set(index, updated);
                saveAll();
                return Optional.of(updated);
            }
        }
        return Optional.empty();
    }

    public synchronized Optional<Punishment> setAppealStatus(int id, String appealStatus) {
        for (int index = 0; index < punishments.size(); index++) {
            Punishment punishment = punishments.get(index);
            if (punishment.id() == id) {
                Punishment updated = new Punishment(punishment.id(), punishment.target(), punishment.targetName(),
                        punishment.staff(), punishment.staffName(), punishment.category(), punishment.offense(),
                        punishment.tier(), punishment.type(), punishment.duration(), punishment.reason(),
                        punishment.evidenceId(), punishment.status(), appealStatus, punishment.createdAt());
                punishments.set(index, updated);
                saveAll();
                return Optional.of(updated);
            }
        }
        return Optional.empty();
    }

    public synchronized Optional<Punishment> latestActivePunishment(UUID target, String... types) {
        return punishmentsFor(target).stream()
                .filter(punishment -> punishment.status().equalsIgnoreCase("ACTIVE"))
                .filter(punishment -> {
                    for (String type : types) {
                        if (punishment.type().equalsIgnoreCase(type)) {
                            return true;
                        }
                    }
                    return false;
                })
                .findFirst();
    }

    public synchronized Evidence addEvidence(UUID target, String targetName, UUID staff, String staffName,
                                             String url, String note, Integer reportId, Integer punishmentId) {
        Evidence item = new Evidence(nextEvidenceId++, target, targetName, staff, staffName, url, note,
                reportId, punishmentId, Instant.now());
        evidence.add(item);
        saveAll();
        return item;
    }

    public synchronized Report addReport(UUID reporter, String reporterName, UUID target, String targetName,
                                         String serverName, String reason) {
        Report report = new Report(nextReportId++, reporter, reporterName, target, targetName, serverName,
                reason, "OPEN", null, Instant.now());
        reports.add(report);
        saveAll();
        return report;
    }

    public synchronized Optional<Report> reportById(int id) {
        return reports.stream().filter(report -> report.id() == id).findFirst();
    }

    public synchronized Optional<Report> claimReport(int id, UUID staff, String staffName) {
        for (int index = 0; index < reports.size(); index++) {
            Report report = reports.get(index);
            if (report.id() == id) {
                Report updated = new Report(report.id(), report.reporter(), report.reporterName(), report.target(),
                        report.targetName(), report.serverName(), report.reason(), "CLAIMED", staff, report.createdAt());
                reports.set(index, updated);
                saveAll();
                return Optional.of(updated);
            }
        }
        return Optional.empty();
    }

    public synchronized Optional<Report> closeReport(int id, UUID staff, String staffName, String reason) {
        for (int index = 0; index < reports.size(); index++) {
            Report report = reports.get(index);
            if (report.id() == id) {
                Report updated = new Report(report.id(), report.reporter(), report.reporterName(), report.target(),
                        report.targetName(), report.serverName(), report.reason() + " | Closed: " + reason,
                        "CLOSED", staff, report.createdAt());
                reports.set(index, updated);
                saveAll();
                return Optional.of(updated);
            }
        }
        return Optional.empty();
    }

    public synchronized StaffNote addNote(UUID target, String targetName, UUID staff, String staffName, String note) {
        StaffNote item = new StaffNote(nextNoteId++, target, targetName, staff, staffName, note, Instant.now());
        notes.add(item);
        saveAll();
        return item;
    }

    public synchronized List<StaffNote> notesFor(UUID target) {
        return notes.stream()
                .filter(note -> note.target().equals(target))
                .sorted(Comparator.comparing(StaffNote::createdAt).reversed())
                .toList();
    }

    public synchronized WatchEntry addWatch(UUID target, String targetName, UUID staff, String staffName, String reason) {
        WatchEntry entry = new WatchEntry(nextWatchId++, target, targetName, staff, staffName, reason, true, Instant.now());
        watches.add(entry);
        saveAll();
        return entry;
    }

    public synchronized List<WatchEntry> activeWatchesFor(UUID target) {
        return watches.stream()
                .filter(watch -> watch.target().equals(target))
                .filter(WatchEntry::active)
                .sorted(Comparator.comparing(WatchEntry::createdAt).reversed())
                .toList();
    }

    public synchronized Optional<WatchEntry> removeWatch(UUID target) {
        for (int index = 0; index < watches.size(); index++) {
            WatchEntry watch = watches.get(index);
            if (watch.target().equals(target) && watch.active()) {
                WatchEntry updated = new WatchEntry(watch.id(), watch.target(), watch.targetName(), watch.staff(),
                        watch.staffName(), watch.reason(), false, watch.createdAt());
                watches.set(index, updated);
                saveAll();
                return Optional.of(updated);
            }
        }
        return Optional.empty();
    }

    public synchronized List<WatchEntry> activeWatches() {
        return watches.stream()
                .filter(WatchEntry::active)
                .sorted(Comparator.comparing(WatchEntry::createdAt).reversed())
                .toList();
    }

    public synchronized AuditLogEntry addAudit(UUID staff, String staffName, String action, String details) {
        AuditLogEntry entry = new AuditLogEntry(nextAuditId++, staff, staffName, action, details, Instant.now());
        auditLog.add(entry);
        saveAll();
        return entry;
    }

    public synchronized List<AuditLogEntry> recentAuditLog() {
        return auditLog.stream()
                .sorted(Comparator.comparing(AuditLogEntry::createdAt).reversed())
                .limit(20)
                .toList();
    }

    public synchronized List<Punishment> punishmentsFor(UUID target) {
        return punishments.stream()
                .filter(punishment -> punishment.target().equals(target))
                .sorted(Comparator.comparing(Punishment::createdAt).reversed())
                .toList();
    }

    public synchronized Optional<Punishment> activeBanFor(UUID target) {
        return punishmentsFor(target).stream()
                .filter(punishment -> punishment.type().equalsIgnoreCase("BAN")
                        || punishment.type().equalsIgnoreCase("TEMPBAN"))
                .filter(this::isActive)
                .findFirst();
    }

    public synchronized Optional<Punishment> activeMuteFor(UUID target) {
        return punishmentsFor(target).stream()
                .filter(punishment -> punishment.type().equalsIgnoreCase("MUTE")
                        || punishment.type().equalsIgnoreCase("TEMPMUTE"))
                .filter(this::isActive)
                .findFirst();
    }

    public synchronized Optional<Punishment> activeIpBanFor(String ip) {
        if (ip == null || ip.isBlank()) {
            return Optional.empty();
        }
        return punishments.stream()
                .filter(punishment -> punishment.type().equalsIgnoreCase("IPBAN")
                        || punishment.type().equalsIgnoreCase("TEMPIPBAN"))
                .filter(this::isActive)
                .filter(punishment -> profileByUuid(punishment.target())
                        .map(profile -> profile.knownIps().contains(ip) || profile.lastIp().equals(ip))
                        .orElse(false))
                .findFirst();
    }

    public synchronized Optional<PlayerProfile> profileByUuid(UUID uuid) {
        return Optional.ofNullable(profiles.get(uuid));
    }

    public synchronized ServerBlacklist addServerBlacklist(UUID target, String targetName, String serverName,
                                                          UUID staff, String staffName, String reason) {
        ServerBlacklist blacklist = new ServerBlacklist(nextServerBlacklistId++, target, targetName, serverName,
                staff, staffName, reason, true, Instant.now());
        serverBlacklists.add(blacklist);
        saveAll();
        return blacklist;
    }

    public synchronized Optional<ServerBlacklist> activeServerBlacklist(UUID target, String serverName) {
        return serverBlacklists.stream()
                .filter(ServerBlacklist::active)
                .filter(blacklist -> blacklist.target().equals(target))
                .filter(blacklist -> blacklist.serverName().equalsIgnoreCase(serverName))
                .findFirst();
    }

    public synchronized List<Punishment> punishmentsFor(UUID target, String category, String offense, Instant since) {
        return punishments.stream()
                .filter(punishment -> punishment.target().equals(target))
                .filter(punishment -> punishment.category().equalsIgnoreCase(category))
                .filter(punishment -> punishment.offense().equalsIgnoreCase(offense))
                .filter(punishment -> punishment.createdAt().isAfter(since))
                .toList();
    }

    public synchronized List<Evidence> evidenceFor(UUID target) {
        return evidence.stream()
                .filter(item -> item.target().equals(target))
                .sorted(Comparator.comparing(Evidence::createdAt).reversed())
                .toList();
    }

    public synchronized Optional<Evidence> evidenceById(int id) {
        return evidence.stream().filter(item -> item.id() == id).findFirst();
    }

    public synchronized List<Report> reportsFor(UUID target) {
        return reports.stream()
                .filter(report -> report.target().equals(target))
                .sorted(Comparator.comparing(Report::createdAt).reversed())
                .toList();
    }

    public synchronized List<Report> reportsHandledBy(UUID staff) {
        return reports.stream()
                .filter(report -> staff != null && staff.equals(report.claimedBy()))
                .toList();
    }

    public synchronized List<Punishment> punishmentsBy(UUID staff) {
        return punishments.stream()
                .filter(punishment -> staff != null && staff.equals(punishment.staff()))
                .toList();
    }

    public synchronized List<Evidence> evidenceBy(UUID staff) {
        return evidence.stream()
                .filter(item -> staff != null && staff.equals(item.staff()))
                .toList();
    }

    public synchronized List<Report> openReports() {
        return reports.stream()
                .filter(report -> report.status().equalsIgnoreCase("OPEN"))
                .sorted(Comparator.comparing(Report::createdAt).reversed())
                .toList();
    }

    public synchronized List<PlayerProfile> linkedProfiles(PlayerProfile profile) {
        return profiles.values().stream()
                .filter(other -> !other.uuid().equals(profile.uuid()))
                .filter(other -> other.knownIps().stream().anyMatch(profile.knownIps()::contains))
                .sorted(Comparator.comparing(PlayerProfile::lastName))
                .toList();
    }

    private void loadProfiles() throws IOException {
        Path path = dataDirectory.resolve("profiles.tsv");
        if (!Files.exists(path)) {
            return;
        }
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split("\t", -1);
            if (parts.length < 9) {
                continue;
            }
            PlayerProfile profile = new PlayerProfile(UUID.fromString(parts[0]), unescape(parts[1]));
            profile.registeredAt(parseInstant(parts[2]));
            profile.lastLoginAt(parseInstant(parts[3]));
            profile.lastLogoutAt(parseInstant(parts[4]));
            profile.lastServer(unescape(parts[5]));
            profile.firstIp(unescape(parts[6]));
            profile.lastIp(unescape(parts[7]));
            for (String ip : parts[8].split(",")) {
                if (!ip.isBlank()) {
                    profile.knownIps().add(unescape(ip));
                }
            }
            profiles.put(profile.uuid(), profile);
        }
    }

    private void saveProfiles() throws IOException {
        List<String> lines = profiles.values().stream()
                .sorted(Comparator.comparing(PlayerProfile::lastName))
                .map(profile -> String.join("\t",
                        profile.uuid().toString(),
                        escape(profile.lastName()),
                        formatInstant(profile.registeredAt()),
                        formatInstant(profile.lastLoginAt()),
                        formatInstant(profile.lastLogoutAt()),
                        escape(profile.lastServer()),
                        escape(profile.firstIp()),
                        escape(profile.lastIp()),
                        profile.knownIps().stream().map(this::escape).collect(Collectors.joining(","))))
                .toList();
        Files.write(dataDirectory.resolve("profiles.tsv"), lines, StandardCharsets.UTF_8);
    }

    private void loadPunishments() throws IOException {
        Path path = dataDirectory.resolve("punishments.tsv");
        if (!Files.exists(path)) {
            return;
        }
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String[] parts = line.split("\t", -1);
            if (parts.length < 13) {
                continue;
            }
            int id = Integer.parseInt(parts[0]);
            String status = parts.length >= 14 ? unescape(parts[12]) : "ACTIVE";
            String appealStatus = parts.length >= 15 ? unescape(parts[13]) : "NONE";
            Instant createdAt = parts.length >= 15 ? parseInstant(parts[14])
                    : parts.length >= 14 ? parseInstant(parts[13]) : parseInstant(parts[12]);
            punishments.add(new Punishment(id, UUID.fromString(parts[1]), unescape(parts[2]),
                    parseUuid(parts[3]), unescape(parts[4]), unescape(parts[5]), unescape(parts[6]),
                    Integer.parseInt(parts[7]), unescape(parts[8]), unescape(parts[9]), unescape(parts[10]),
                    parseInteger(parts[11]), status, appealStatus, createdAt));
            nextPunishmentId = Math.max(nextPunishmentId, id + 1);
        }
    }

    private void savePunishments() throws IOException {
        List<String> lines = punishments.stream().map(punishment -> String.join("\t",
                Integer.toString(punishment.id()),
                punishment.target().toString(),
                escape(punishment.targetName()),
                formatUuid(punishment.staff()),
                escape(punishment.staffName()),
                escape(punishment.category()),
                escape(punishment.offense()),
                Integer.toString(punishment.tier()),
                escape(punishment.type()),
                escape(punishment.duration()),
                escape(punishment.reason()),
                punishment.evidenceId() == null ? "" : Integer.toString(punishment.evidenceId()),
                escape(punishment.status()),
                escape(punishment.appealStatus()),
                formatInstant(punishment.createdAt()))).toList();
        Files.write(dataDirectory.resolve("punishments.tsv"), lines, StandardCharsets.UTF_8);
    }

    private void loadEvidence() throws IOException {
        Path path = dataDirectory.resolve("evidence.tsv");
        if (!Files.exists(path)) {
            return;
        }
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String[] parts = line.split("\t", -1);
            if (parts.length < 11) {
                continue;
            }
            int id = Integer.parseInt(parts[0]);
            evidence.add(new Evidence(id, UUID.fromString(parts[1]), unescape(parts[2]), parseUuid(parts[3]),
                    unescape(parts[4]), unescape(parts[5]), unescape(parts[6]), parseInteger(parts[7]),
                    parseInteger(parts[8]), parseInstant(parts[10])));
            nextEvidenceId = Math.max(nextEvidenceId, id + 1);
        }
    }

    private void saveEvidence() throws IOException {
        List<String> lines = evidence.stream().map(item -> String.join("\t",
                Integer.toString(item.id()),
                item.target().toString(),
                escape(item.targetName()),
                formatUuid(item.staff()),
                escape(item.staffName()),
                escape(item.url()),
                escape(item.note()),
                item.reportId() == null ? "" : Integer.toString(item.reportId()),
                item.punishmentId() == null ? "" : Integer.toString(item.punishmentId()),
                "",
                formatInstant(item.createdAt()))).toList();
        Files.write(dataDirectory.resolve("evidence.tsv"), lines, StandardCharsets.UTF_8);
    }

    private void loadReports() throws IOException {
        Path path = dataDirectory.resolve("reports.tsv");
        if (!Files.exists(path)) {
            return;
        }
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String[] parts = line.split("\t", -1);
            if (parts.length < 11) {
                continue;
            }
            int id = Integer.parseInt(parts[0]);
            reports.add(new Report(id, parseUuid(parts[1]), unescape(parts[2]), UUID.fromString(parts[3]),
                    unescape(parts[4]), unescape(parts[5]), unescape(parts[6]), unescape(parts[7]),
                    parseUuid(parts[8]), parseInstant(parts[10])));
            nextReportId = Math.max(nextReportId, id + 1);
        }
    }

    private void saveReports() throws IOException {
        List<String> lines = reports.stream().map(report -> String.join("\t",
                Integer.toString(report.id()),
                formatUuid(report.reporter()),
                escape(report.reporterName()),
                report.target().toString(),
                escape(report.targetName()),
                escape(report.serverName()),
                escape(report.reason()),
                escape(report.status()),
                formatUuid(report.claimedBy()),
                "",
                formatInstant(report.createdAt()))).toList();
        Files.write(dataDirectory.resolve("reports.tsv"), lines, StandardCharsets.UTF_8);
    }

    private void loadNotes() throws IOException {
        Path path = dataDirectory.resolve("notes.tsv");
        if (!Files.exists(path)) {
            return;
        }
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String[] parts = line.split("\t", -1);
            if (parts.length < 7) {
                continue;
            }
            int id = Integer.parseInt(parts[0]);
            notes.add(new StaffNote(id, UUID.fromString(parts[1]), unescape(parts[2]), parseUuid(parts[3]),
                    unescape(parts[4]), unescape(parts[5]), parseInstant(parts[6])));
            nextNoteId = Math.max(nextNoteId, id + 1);
        }
    }

    private void saveNotes() throws IOException {
        List<String> lines = notes.stream().map(note -> String.join("\t",
                Integer.toString(note.id()),
                note.target().toString(),
                escape(note.targetName()),
                formatUuid(note.staff()),
                escape(note.staffName()),
                escape(note.note()),
                formatInstant(note.createdAt()))).toList();
        Files.write(dataDirectory.resolve("notes.tsv"), lines, StandardCharsets.UTF_8);
    }

    private void loadWatches() throws IOException {
        Path path = dataDirectory.resolve("watches.tsv");
        if (!Files.exists(path)) {
            return;
        }
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String[] parts = line.split("\t", -1);
            if (parts.length < 8) {
                continue;
            }
            int id = Integer.parseInt(parts[0]);
            watches.add(new WatchEntry(id, UUID.fromString(parts[1]), unescape(parts[2]), parseUuid(parts[3]),
                    unescape(parts[4]), unescape(parts[5]), Boolean.parseBoolean(parts[6]), parseInstant(parts[7])));
            nextWatchId = Math.max(nextWatchId, id + 1);
        }
    }

    private void saveWatches() throws IOException {
        List<String> lines = watches.stream().map(watch -> String.join("\t",
                Integer.toString(watch.id()),
                watch.target().toString(),
                escape(watch.targetName()),
                formatUuid(watch.staff()),
                escape(watch.staffName()),
                escape(watch.reason()),
                Boolean.toString(watch.active()),
                formatInstant(watch.createdAt()))).toList();
        Files.write(dataDirectory.resolve("watches.tsv"), lines, StandardCharsets.UTF_8);
    }

    private void loadAuditLog() throws IOException {
        Path path = dataDirectory.resolve("audit.tsv");
        if (!Files.exists(path)) {
            return;
        }
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String[] parts = line.split("\t", -1);
            if (parts.length < 6) {
                continue;
            }
            int id = Integer.parseInt(parts[0]);
            auditLog.add(new AuditLogEntry(id, parseUuid(parts[1]), unescape(parts[2]), unescape(parts[3]),
                    unescape(parts[4]), parseInstant(parts[5])));
            nextAuditId = Math.max(nextAuditId, id + 1);
        }
    }

    private void saveAuditLog() throws IOException {
        List<String> lines = auditLog.stream().map(entry -> String.join("\t",
                Integer.toString(entry.id()),
                formatUuid(entry.staff()),
                escape(entry.staffName()),
                escape(entry.action()),
                escape(entry.details()),
                formatInstant(entry.createdAt()))).toList();
        Files.write(dataDirectory.resolve("audit.tsv"), lines, StandardCharsets.UTF_8);
    }

    private void loadServerBlacklists() throws IOException {
        Path path = dataDirectory.resolve("server-blacklists.tsv");
        if (!Files.exists(path)) {
            return;
        }
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String[] parts = line.split("\t", -1);
            if (parts.length < 9) {
                continue;
            }
            int id = Integer.parseInt(parts[0]);
            serverBlacklists.add(new ServerBlacklist(id, UUID.fromString(parts[1]), unescape(parts[2]),
                    unescape(parts[3]), parseUuid(parts[4]), unescape(parts[5]), unescape(parts[6]),
                    Boolean.parseBoolean(parts[7]), parseInstant(parts[8])));
            nextServerBlacklistId = Math.max(nextServerBlacklistId, id + 1);
        }
    }

    private void saveServerBlacklists() throws IOException {
        List<String> lines = serverBlacklists.stream().map(blacklist -> String.join("\t",
                Integer.toString(blacklist.id()),
                blacklist.target().toString(),
                escape(blacklist.targetName()),
                escape(blacklist.serverName()),
                formatUuid(blacklist.staff()),
                escape(blacklist.staffName()),
                escape(blacklist.reason()),
                Boolean.toString(blacklist.active()),
                formatInstant(blacklist.createdAt()))).toList();
        Files.write(dataDirectory.resolve("server-blacklists.tsv"), lines, StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n").replace(",", "\\c");
    }

    private String unescape(String value) {
        return value.replace("\\c", ",").replace("\\n", "\n").replace("\\t", "\t").replace("\\\\", "\\");
    }

    private Instant parseInstant(String value) {
        return value == null || value.isBlank() ? null : Instant.parse(value);
    }

    private String formatInstant(Instant instant) {
        return instant == null ? "" : instant.toString();
    }

    private UUID parseUuid(String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }

    private String formatUuid(UUID uuid) {
        return uuid == null ? "" : uuid.toString();
    }

    private Integer parseInteger(String value) {
        return value == null || value.isBlank() ? null : Integer.parseInt(value);
    }

    private boolean isActive(Punishment punishment) {
        if (!punishment.status().equalsIgnoreCase("ACTIVE")) {
            return false;
        }
        if (punishment.duration().equalsIgnoreCase("permanent")) {
            return true;
        }
        long seconds = durationSeconds(punishment.duration());
        return seconds <= 0 || punishment.createdAt().plusSeconds(seconds).isAfter(Instant.now());
    }

    private long durationSeconds(String duration) {
        if (duration == null || duration.isBlank() || duration.equalsIgnoreCase("none")) {
            return 0;
        }
        String trimmed = duration.trim().toLowerCase();
        try {
            long amount = Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
            char unit = trimmed.charAt(trimmed.length() - 1);
            return switch (unit) {
                case 'm' -> amount * 60;
                case 'h' -> amount * 3600;
                case 'd' -> amount * 86400;
                default -> 0;
            };
        } catch (RuntimeException ignored) {
            return 0;
        }
    }
}
