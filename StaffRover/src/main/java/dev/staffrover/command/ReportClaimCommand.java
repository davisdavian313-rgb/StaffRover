package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;

public final class ReportClaimCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public ReportClaimCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.report.claim")) {
            return;
        }
        if (invocation.arguments().length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /reportclaim <id>"));
            return;
        }
        try {
            int id = Integer.parseInt(invocation.arguments()[0]);
            plugin.storage().claimReport(id, plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()))
                    .ifPresentOrElse(report -> {
                        plugin.storage().addAudit(plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()),
                                "REPORT_CLAIM", "Claimed report #" + id);
                        invocation.source().sendMessage(CommandUtil.success("Claimed report #" + id + "."));
                    }, () -> invocation.source().sendMessage(CommandUtil.error("Unknown report id.")));
        } catch (NumberFormatException exception) {
            invocation.source().sendMessage(CommandUtil.error("Report id must be a number."));
        }
    }
}
