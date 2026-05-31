package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.Punishment;

public final class PunishmentCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public PunishmentCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.punishment.revoke")) {
            return;
        }
        String[] args = invocation.arguments();
        if (args.length < 2 || !args[0].equalsIgnoreCase("revoke")) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /punishment revoke <id>"));
            return;
        }
        try {
            int id = Integer.parseInt(args[1]);
            plugin.storage().punishmentById(id).ifPresentOrElse(punishment -> revoke(invocation, punishment),
                    () -> invocation.source().sendMessage(CommandUtil.error("Unknown punishment id.")));
        } catch (NumberFormatException exception) {
            invocation.source().sendMessage(CommandUtil.error("Punishment id must be a number."));
        }
    }

    private void revoke(Invocation invocation, Punishment punishment) {
        if (punishment.status().equalsIgnoreCase("REVOKED")) {
            invocation.source().sendMessage(CommandUtil.error("Punishment #" + punishment.id() + " is already revoked."));
            return;
        }
        plugin.storage().revokePunishment(punishment.id()).ifPresent(updated -> {
            plugin.storage().addAudit(plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()),
                    "PUNISHMENT_REVOKE", "Revoked punishment #" + updated.id() + " for " + updated.targetName());
            invocation.source().sendMessage(CommandUtil.success("Revoked punishment #" + updated.id() + " for " + updated.targetName() + "."));
        });
    }
}
