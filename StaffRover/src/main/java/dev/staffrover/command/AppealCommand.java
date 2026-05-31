package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;

import java.util.Set;

public final class AppealCommand implements SimpleCommand {
    private static final Set<String> STATUSES = Set.of("UPHELD", "REDUCED", "ACCEPTED", "DENIED");
    private final StaffRoverPlugin plugin;

    public AppealCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.appeal.manage")) {
            return;
        }
        String[] args = invocation.arguments();
        if (args.length < 2) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /appeal <punishmentId> <upheld|reduced|accepted|denied>"));
            return;
        }
        String status = args[1].toUpperCase();
        if (!STATUSES.contains(status)) {
            invocation.source().sendMessage(CommandUtil.error("Appeal status must be upheld, reduced, accepted, or denied."));
            return;
        }
        try {
            int id = Integer.parseInt(args[0]);
            plugin.storage().setAppealStatus(id, status).ifPresentOrElse(punishment -> {
                if (status.equals("ACCEPTED")) {
                    plugin.storage().revokePunishment(id);
                }
                plugin.storage().addAudit(plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()),
                        "APPEAL_STATUS", "Punishment #" + id + " marked " + status);
                invocation.source().sendMessage(CommandUtil.success("Punishment #" + id + " appeal marked " + status.toLowerCase() + "."));
            }, () -> invocation.source().sendMessage(CommandUtil.error("Unknown punishment id.")));
        } catch (NumberFormatException exception) {
            invocation.source().sendMessage(CommandUtil.error("Punishment id must be a number."));
        }
    }
}
