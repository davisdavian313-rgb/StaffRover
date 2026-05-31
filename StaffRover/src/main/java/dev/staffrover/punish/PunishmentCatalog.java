package dev.staffrover.punish;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PunishmentCatalog {
    private final List<Category> categories = new ArrayList<>();

    public PunishmentCatalog() {
        seedDefaults();
    }

    public List<Category> categories() {
        return categories;
    }

    public Optional<Category> category(String id) {
        return categories.stream().filter(category -> category.id().equalsIgnoreCase(id)).findFirst();
    }

    public Optional<Offense> offense(String categoryId, String offenseId) {
        return category(categoryId).flatMap(category -> category.offenses().stream()
                .filter(offense -> offense.id().equalsIgnoreCase(offenseId))
                .findFirst());
    }

    private void seedDefaults() {
        categories.add(new Category("cheating", "Cheating", List.of(
                offense("fly", "Fly Hacking", true, 90, List.of(
                        tier("TEMPBAN", "14d", "Cheating - Fly Hacking", "tempban {player} 14d Cheating - Fly Hacking"),
                        tier("TEMPBAN", "30d", "Cheating - Fly Hacking, second offense", "tempban {player} 30d Cheating - Fly Hacking"),
                        tier("BAN", "permanent", "Cheating - Fly Hacking, repeated offense", "ban {player} Cheating - Repeated Fly Hacking"))),
                offense("killaura", "Kill Aura", true, 90, List.of(
                        tier("TEMPBAN", "30d", "Cheating - Kill Aura", "tempban {player} 30d Cheating - Kill Aura"),
                        tier("BAN", "permanent", "Cheating - Kill Aura, second offense", "ban {player} Cheating - Repeated Kill Aura"))),
                offense("xray", "X-Ray", true, 90, List.of(
                        tier("TEMPBAN", "14d", "Cheating - X-Ray", "tempban {player} 14d Cheating - X-Ray"),
                        tier("TEMPBAN", "30d", "Cheating - X-Ray, second offense", "tempban {player} 30d Cheating - X-Ray"),
                        tier("BAN", "permanent", "Cheating - Repeated X-Ray", "ban {player} Cheating - Repeated X-Ray"))),
                offense("autoclicker", "Auto Clicker", true, 60, List.of(
                        tier("TEMPBAN", "7d", "Cheating - Auto Clicker", "tempban {player} 7d Cheating - Auto Clicker"),
                        tier("TEMPBAN", "14d", "Cheating - Auto Clicker, second offense", "tempban {player} 14d Cheating - Auto Clicker"),
                        tier("TEMPBAN", "30d", "Cheating - Repeated Auto Clicker", "tempban {player} 30d Cheating - Auto Clicker"))))));

        categories.add(new Category("chat", "Chat Offense", List.of(
                offense("spam", "Spam", false, 30, List.of(
                        tier("WARN", "none", "Chat Offense - Spam", "warn {player} Chat Offense - Spam"),
                        tier("MUTE", "30m", "Chat Offense - Spam, second offense", "mute {player} 30m Chat Offense - Spam"),
                        tier("MUTE", "2h", "Chat Offense - Repeated Spam", "mute {player} 2h Chat Offense - Repeated Spam"),
                        tier("MUTE", "1d", "Chat Offense - Continued Spam", "mute {player} 1d Chat Offense - Continued Spam"))),
                offense("toxicity", "Toxicity", false, 60, List.of(
                        tier("MUTE", "2h", "Chat Offense - Toxicity", "mute {player} 2h Chat Offense - Toxicity"),
                        tier("MUTE", "1d", "Chat Offense - Repeated Toxicity", "mute {player} 1d Chat Offense - Toxicity"),
                        tier("MUTE", "7d", "Chat Offense - Continued Toxicity", "mute {player} 7d Chat Offense - Toxicity"))),
                offense("hate_speech", "Hate Speech", true, 180, List.of(
                        tier("MUTE", "7d", "Chat Offense - Hate Speech", "mute {player} 7d Chat Offense - Hate Speech"),
                        tier("BAN", "30d", "Chat Offense - Repeated Hate Speech", "tempban {player} 30d Chat Offense - Hate Speech"))),
                offense("threats", "Threats / Doxxing", true, 365, List.of(
                        tier("BAN", "30d", "Severe Chat Offense - Threats or Doxxing", "tempban {player} 30d Severe Chat Offense - Threats or Doxxing"),
                        tier("BAN", "permanent", "Severe Chat Offense - Repeated Threats or Doxxing", "ban {player} Severe Chat Offense - Threats or Doxxing"))))));

        categories.add(new Category("gameplay", "Gameplay Offense", List.of(
                offense("combat_logging", "Combat Logging", false, 30, List.of(
                        tier("WARN", "none", "Gameplay Offense - Combat Logging", "warn {player} Gameplay Offense - Combat Logging"),
                        tier("TEMPBAN", "1d", "Gameplay Offense - Repeated Combat Logging", "tempban {player} 1d Gameplay Offense - Combat Logging"),
                        tier("TEMPBAN", "3d", "Gameplay Offense - Continued Combat Logging", "tempban {player} 3d Gameplay Offense - Combat Logging"))),
                offense("teaming", "Teaming in Solo Mode", true, 60, List.of(
                        tier("TEMPBAN", "3d", "Gameplay Offense - Teaming in Solo Mode", "tempban {player} 3d Gameplay Offense - Teaming"),
                        tier("TEMPBAN", "7d", "Gameplay Offense - Repeated Teaming", "tempban {player} 7d Gameplay Offense - Teaming"))))));

        categories.add(new Category("griefing", "Griefing / Stealing", List.of(
                offense("minor_grief", "Minor Griefing", true, 60, List.of(
                        tier("TEMPBAN", "1d", "Griefing - Minor Griefing", "tempban {player} 1d Griefing - Minor"),
                        tier("TEMPBAN", "7d", "Griefing - Repeated Minor Griefing", "tempban {player} 7d Griefing - Minor"))),
                offense("major_grief", "Major Griefing", true, 180, List.of(
                        tier("TEMPBAN", "14d", "Griefing - Major Griefing", "tempban {player} 14d Griefing - Major"),
                        tier("BAN", "permanent", "Griefing - Repeated Major Griefing", "ban {player} Griefing - Major"))))));

        categories.add(new Category("exploiting", "Exploiting / Bug Abuse", List.of(
                offense("bug_abuse", "Bug Abuse", true, 180, List.of(
                        tier("TEMPBAN", "7d", "Exploiting - Bug Abuse", "tempban {player} 7d Exploiting - Bug Abuse"),
                        tier("TEMPBAN", "30d", "Exploiting - Repeated Bug Abuse", "tempban {player} 30d Exploiting - Bug Abuse"),
                        tier("BAN", "permanent", "Exploiting - Continued Bug Abuse", "ban {player} Exploiting - Bug Abuse"))))));

        categories.add(new Category("evasion", "Ban Evasion", List.of(
                offense("likely_evasion", "Likely Ban Evasion", true, 365, List.of(
                        tier("FREEZE", "review", "Likely Ban Evasion - Staff Review", "freeze {player} Likely Ban Evasion - Staff Review"))),
                offense("confirmed_evasion", "Confirmed Ban Evasion", true, 3650, List.of(
                        tier("BAN", "permanent", "Confirmed Ban Evasion", "ban {player} Confirmed Ban Evasion"),
                        tier("BAN", "permanent", "Repeated Ban Evasion", "ban {player} Repeated Ban Evasion"))))));

        categories.add(new Category("identity", "Inappropriate Skin / Name", List.of(
                offense("bad_name", "Inappropriate Name", false, 90, List.of(
                        tier("KICK", "none", "Inappropriate Name - Change Required", "kick {player} Inappropriate Name - Change Required"),
                        tier("TEMPBAN", "1d", "Inappropriate Name - Repeated", "tempban {player} 1d Inappropriate Name"))),
                offense("bad_skin", "Inappropriate Skin", false, 90, List.of(
                        tier("KICK", "none", "Inappropriate Skin - Change Required", "kick {player} Inappropriate Skin - Change Required"),
                        tier("TEMPBAN", "1d", "Inappropriate Skin - Repeated", "tempban {player} 1d Inappropriate Skin"))))));

        categories.add(new Category("advertising", "Advertising", List.of(
                offense("minor_ad", "Minor Advertising", false, 60, List.of(
                        tier("MUTE", "1d", "Advertising - Minor Advertising", "mute {player} 1d Advertising - Minor"),
                        tier("MUTE", "7d", "Advertising - Repeated Minor Advertising", "mute {player} 7d Advertising - Minor"))),
                offense("server_ad", "Server Advertising", true, 180, List.of(
                        tier("BAN", "7d", "Advertising - Server Advertising", "tempban {player} 7d Advertising - Server"),
                        tier("BAN", "30d", "Advertising - Repeated Server Advertising", "tempban {player} 30d Advertising - Server"))),
                offense("malicious_link", "Malicious Links", true, 365, List.of(
                        tier("BAN", "permanent", "Advertising - Malicious Links", "ban {player} Advertising - Malicious Links"))))));

        categories.add(new Category("staff_disrespect", "Staff Disrespect", List.of(
                offense("minor_disrespect", "Minor Staff Disrespect", false, 30, List.of(
                        tier("WARN", "none", "Staff Disrespect", "warn {player} Staff Disrespect"),
                        tier("MUTE", "2h", "Repeated Staff Disrespect", "mute {player} 2h Staff Disrespect"))),
                offense("severe_disrespect", "Severe Staff Harassment", true, 90, List.of(
                        tier("MUTE", "1d", "Severe Staff Harassment", "mute {player} 1d Staff Harassment"),
                        tier("TEMPBAN", "7d", "Repeated Staff Harassment", "tempban {player} 7d Staff Harassment"))))));
    }

    private Offense offense(String id, String display, boolean evidenceRequired, int escalationWindowDays, List<Tier> tiers) {
        return new Offense(id, display, evidenceRequired, escalationWindowDays, tiers);
    }

    private Tier tier(String type, String duration, String reason, String command) {
        return new Tier(type, duration, reason, command);
    }

    public record Category(String id, String displayName, List<Offense> offenses) {
    }

    public record Offense(String id, String displayName, boolean evidenceRequired, int escalationWindowDays, List<Tier> tiers) {
        public Tier tierForPreviousCount(long previousCount) {
            int index = (int) Math.min(previousCount, tiers.size() - 1);
            return tiers.get(index);
        }
    }

    public record Tier(String type, String duration, String reason, String command) {
    }
}
