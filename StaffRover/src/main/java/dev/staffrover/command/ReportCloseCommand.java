package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;

public final class ReportCloseCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public ReportCloseCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.report.close")) {
            return;
        }
        if (invocation.arguments().length < 2) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /reportclose <id> <reason>"));
            return;
        }
        try {
            int id = Integer.parseInt(invocation.arguments()[0]);
            String reason = CommandUtil.join(invocation.arguments(), 1);
            plugin.storage().closeReport(id, plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()), reason)
                    .ifPresentOrElse(report -> {
                        plugin.storage().addAudit(plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()),
                                "REPORT_CLOSE", "Closed report #" + id + ": " + reason);
                        invocation.source().sendMessage(CommandUtil.success("Closed report #" + id + "."));
                    }, () -> invocation.source().sendMessage(CommandUtil.error("Unknown report id.")));
        } catch (NumberFormatException exception) {
            invocation.source().sendMessage(CommandUtil.error("Report id must be a number."));
        }
    }
}
