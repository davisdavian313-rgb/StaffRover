package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.Report;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ReportCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public ReportCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (!(invocation.source() instanceof Player reporter)) {
            invocation.source().sendMessage(CommandUtil.error("Only players can use /report."));
            return;
        }
        if (args.length < 2) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /report <player> <reason>"));
            return;
        }
        Player target = plugin.server().getPlayer(args[0]).orElse(null);
        if (target == null) {
            invocation.source().sendMessage(CommandUtil.error("That player is not online."));
            return;
        }
        String serverName = reporter.getCurrentServer()
                .map(server -> server.getServerInfo().getName())
                .orElse("unknown");
        Report report = plugin.storage().addReport(reporter.getUniqueId(), reporter.getUsername(), target.getUniqueId(),
                target.getUsername(), serverName, CommandUtil.join(args, 1));
        plugin.storage().addAudit(reporter.getUniqueId(), reporter.getUsername(), "REPORT_CREATE",
                "Report #" + report.id() + " against " + target.getUsername());
        invocation.source().sendMessage(CommandUtil.success("Report #" + report.id() + " submitted."));
        plugin.broadcastStaff(Component.text("[Report #" + report.id() + "] ", NamedTextColor.RED)
                .append(Component.text(reporter.getUsername() + " reported " + target.getUsername(), NamedTextColor.YELLOW))
                .append(Component.text(" on " + serverName + ": " + report.reason() + " ", NamedTextColor.GRAY))
                .append(Component.text("[Punish]", NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.runCommand("/punish " + target.getUsername()))),
                "staffrover.report.view");
    }
}
