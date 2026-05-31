package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.AuditLogEntry;

public final class AuditLogCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public AuditLogCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.audit.view")) {
            return;
        }
        invocation.source().sendMessage(CommandUtil.info("Recent STAFF audit log:"));
        for (AuditLogEntry entry : plugin.storage().recentAuditLog()) {
            invocation.source().sendMessage(CommandUtil.id(entry.id())
                    .append(CommandUtil.name(entry.action()))
                    .append(CommandUtil.detail(" by "))
                    .append(CommandUtil.name(entry.staffName()))
                    .append(CommandUtil.detail(": " + entry.details())));
        }
    }
}
