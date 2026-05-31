package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.staffrover.StaffRoverPlugin;

import java.util.UUID;

public final class StaffStatsCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public StaffStatsCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.staffstats")) {
            return;
        }
        UUID staff = plugin.senderUuid(invocation.source());
        String name = plugin.senderName(invocation.source());
        if (invocation.arguments().length >= 1) {
            Player target = plugin.server().getPlayer(invocation.arguments()[0]).orElse(null);
            if (target != null) {
                staff = target.getUniqueId();
                name = target.getUsername();
            }
        }
        invocation.source().sendMessage(CommandUtil.prefix()
                .append(CommandUtil.detail("Staff stats: "))
                .append(CommandUtil.name(name)));
        line(invocation, "Punishments issued", plugin.storage().punishmentsBy(staff).size());
        line(invocation, "Evidence added", plugin.storage().evidenceBy(staff).size());
        line(invocation, "Reports handled", plugin.storage().reportsHandledBy(staff).size());
    }

    private void line(Invocation invocation, String label, int count) {
        invocation.source().sendMessage(CommandUtil.label(label, Integer.toString(count)));
    }
}
