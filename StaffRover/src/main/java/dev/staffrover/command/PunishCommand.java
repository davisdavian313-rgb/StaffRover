package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.Evidence;
import dev.staffrover.model.PlayerProfile;
import dev.staffrover.model.Punishment;
import dev.staffrover.punish.PunishmentCatalog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class PunishCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public PunishCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.punish")) {
            return;
        }
        String[] args = invocation.arguments();
        if (args.length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /punish <player> [category] [offense] [evidenceId|confirm]"));
            return;
        }
        Optional<PlayerProfile> target = plugin.findProfile(args[0]);
        if (target.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("That player is not online and has no STAFF profile yet."));
            return;
        }
        if (args.length == 1) {
            showCategories(invocation, target.get());
            return;
        }
        if (args.length == 2) {
            showOffenses(invocation, target.get(), args[1]);
            return;
        }
        if (args.length >= 3) {
            Integer evidenceId = null;
            boolean confirmed = false;
            if (args.length >= 4) {
                if (args[3].equalsIgnoreCase("confirm")) {
                    confirmed = true;
                } else {
                    try {
                        evidenceId = Integer.parseInt(args[3]);
                    } catch (NumberFormatException ignored) {
                        invocation.source().sendMessage(CommandUtil.error("Evidence id must be a number, or use confirm."));
                        return;
                    }
                }
            }
            if (args.length >= 5 && args[4].equalsIgnoreCase("confirm")) {
                confirmed = true;
            }
            showOrRun(invocation, target.get(), args[1], args[2], evidenceId, confirmed);
        }
    }

    private void showCategories(Invocation invocation, PlayerProfile target) {
        invocation.source().sendMessage(Component.text(" "));
        invocation.source().sendMessage(CommandUtil.prefix()
                .append(Component.text("Punish menu for ", NamedTextColor.GRAY))
                .append(Component.text(target.lastName(), NamedTextColor.YELLOW)));
        for (PunishmentCatalog.Category category : plugin.catalog().categories()) {
            invocation.source().sendMessage(Component.text(" - ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(category.displayName(), NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.runCommand("/punish " + target.lastName() + " " + category.id())))
                    .append(Component.text(" (" + category.offenses().size() + " offenses)", NamedTextColor.GRAY)));
        }
    }

    private void showOffenses(Invocation invocation, PlayerProfile target, String categoryId) {
        Optional<PunishmentCatalog.Category> category = plugin.catalog().category(categoryId);
        if (category.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown punishment category."));
            return;
        }
        invocation.source().sendMessage(CommandUtil.prefix()
                .append(Component.text(category.get().displayName() + " offenses for ", NamedTextColor.GRAY))
                .append(Component.text(target.lastName(), NamedTextColor.YELLOW)));
        for (PunishmentCatalog.Offense offense : category.get().offenses()) {
            long previous = previousCount(target, categoryId, offense);
            PunishmentCatalog.Tier tier = offense.tierForPreviousCount(previous);
            invocation.source().sendMessage(Component.text(" - ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(offense.displayName(), NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.runCommand("/punish " + target.lastName() + " " + categoryId + " " + offense.id())))
                    .append(Component.text(" | next: " + tier.type() + " " + tier.duration(), NamedTextColor.GRAY))
                    .append(Component.text(" | tier " + (Math.min(previous + 1, offense.tiers().size())) + "/" + offense.tiers().size(), NamedTextColor.DARK_AQUA))
                    .append(offense.evidenceRequired() ? Component.text(" | evidence required", NamedTextColor.RED) : Component.empty()));
        }
    }

    private void showOrRun(Invocation invocation, PlayerProfile target, String categoryId, String offenseId,
                           Integer evidenceId, boolean confirmed) {
        Optional<PunishmentCatalog.Offense> offenseOptional = plugin.catalog().offense(categoryId, offenseId);
        if (offenseOptional.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown offense."));
            return;
        }
        PunishmentCatalog.Offense offense = offenseOptional.get();
        if (offense.evidenceRequired() && evidenceId == null && !invocation.source().hasPermission("staffrover.evidence.bypass")) {
            invocation.source().sendMessage(CommandUtil.error("This offense requires evidence. Use /evidence add " + target.lastName() + " <url> <note>, then /punish " + target.lastName() + " " + categoryId + " " + offenseId + " <evidenceId>"));
            List<Evidence> items = plugin.storage().evidenceFor(target.uuid()).stream().limit(3).toList();
            for (Evidence item : items) {
                invocation.source().sendMessage(Component.text("Evidence #" + item.id() + ": " + item.url(), NamedTextColor.GRAY)
                        .clickEvent(ClickEvent.runCommand("/punish " + target.lastName() + " " + categoryId + " " + offenseId + " " + item.id())));
            }
            return;
        }
        long previous = previousCount(target, categoryId, offense);
        PunishmentCatalog.Tier tier = offense.tierForPreviousCount(previous);
        int tierNumber = (int) Math.min(previous + 1, offense.tiers().size());
        if (!confirmed) {
            invocation.source().sendMessage(CommandUtil.prefix()
                    .append(Component.text("Confirm ", NamedTextColor.GRAY))
                    .append(Component.text(offense.displayName(), NamedTextColor.YELLOW))
                    .append(Component.text(" for " + target.lastName() + ": " + tier.type() + " " + tier.duration(), NamedTextColor.GRAY)));
            invocation.source().sendMessage(Component.text("[Confirm punishment]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/punish " + target.lastName() + " " + categoryId + " " + offenseId
                            + (evidenceId == null ? "" : " " + evidenceId) + " confirm")));
            invocation.source().sendMessage(Component.text("Reason: " + tier.reason(), NamedTextColor.GRAY));
            return;
        }
        Punishment punishment = plugin.storage().addPunishment(target.uuid(), target.lastName(), plugin.senderUuid(invocation.source()),
                plugin.senderName(invocation.source()), categoryId, offenseId, tierNumber, tier.type(), tier.duration(),
                tier.reason(), evidenceId);
        plugin.applyPunishmentAction(punishment);
        plugin.storage().addAudit(plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()),
                "PUNISH", target.lastName() + " #" + punishment.id() + " " + tier.reason());
        String renderedCommand = tier.command().replace("{player}", target.lastName()).replace("{reason}", tier.reason());
        plugin.server().getCommandManager().executeAsync(plugin.server().getConsoleCommandSource(), renderedCommand);
        invocation.source().sendMessage(CommandUtil.success("Punishment #" + punishment.id() + " logged and executed: " + renderedCommand));
        plugin.broadcastStaff(CommandUtil.prefix()
                .append(CommandUtil.name(target.lastName()))
                .append(CommandUtil.detail(" punished by "))
                .append(CommandUtil.name(plugin.senderName(invocation.source())))
                .append(CommandUtil.detail(": " + tier.reason())),
                "staffrover.punish.notify");
    }

    private long previousCount(PlayerProfile target, String categoryId, PunishmentCatalog.Offense offense) {
        Instant since = Instant.now().minusSeconds(offense.escalationWindowDays() * 86400L);
        return plugin.storage().punishmentsFor(target.uuid(), categoryId, offense.id(), since).size();
    }
}
