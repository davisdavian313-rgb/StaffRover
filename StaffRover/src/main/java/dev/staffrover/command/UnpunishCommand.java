package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.PlayerProfile;
import dev.staffrover.model.Punishment;

import java.util.Optional;

public final class UnpunishCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;
    private final String permission;
    private final String label;
    private final String[] types;

    public UnpunishCommand(StaffRoverPlugin plugin, String permission, String label, String... types) {
        this.plugin = plugin;
        this.permission = permission;
        this.label = label;
        this.types = types;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), permission)) {
            return;
        }
        if (invocation.arguments().length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /" + label + " <player>"));
            return;
        }
        Optional<PlayerProfile> target = plugin.findProfile(invocation.arguments()[0]);
        if (target.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown player."));
            return;
        }
        Optional<Punishment> punishment = plugin.storage().latestActivePunishment(target.get().uuid(), types);
        if (punishment.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("No active matching punishment found for " + target.get().lastName() + "."));
            return;
        }
        plugin.storage().revokePunishment(punishment.get().id()).ifPresent(updated -> {
            plugin.storage().addAudit(plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()),
                    label.toUpperCase(), "Revoked punishment #" + updated.id() + " for " + updated.targetName());
            invocation.source().sendMessage(CommandUtil.success("Revoked " + updated.type().toLowerCase() + " punishment #" + updated.id() + " for " + updated.targetName() + "."));
        });
    }
}
