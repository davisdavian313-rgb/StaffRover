package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.Report;
import net.kyori.adventure.text.event.ClickEvent;

public final class ReportsCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public ReportsCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.report.view")) {
            return;
        }
        invocation.source().sendMessage(CommandUtil.info("Open reports:"));
        for (Report report : plugin.storage().openReports().stream().limit(10).toList()) {
            invocation.source().sendMessage(CommandUtil.id(report.id())
                    .append(CommandUtil.name(report.targetName()))
                    .append(CommandUtil.detail(" reported by "))
                    .append(CommandUtil.name(report.reporterName()))
                    .append(CommandUtil.detail(": " + report.reason() + " "))
                    .append(CommandUtil.action("[Claim] ")
                            .clickEvent(ClickEvent.runCommand("/reportclaim " + report.id())))
                    .append(CommandUtil.danger("[Punish]")
                            .clickEvent(ClickEvent.runCommand("/punish " + report.targetName()))));
        }
    }
}
