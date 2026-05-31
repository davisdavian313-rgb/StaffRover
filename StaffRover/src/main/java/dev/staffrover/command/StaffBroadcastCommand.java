package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class StaffBroadcastCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public StaffBroadcastCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.staffbroadcast")) {
            return;
        }
        if (invocation.arguments().length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /staffbroadcast <message>"));
            return;
        }
        plugin.broadcastStaff(Component.text("[Staff Notice] ", NamedTextColor.GOLD)
                .append(Component.text(CommandUtil.join(invocation.arguments(), 0), NamedTextColor.WHITE)),
                "staffrover.staff");
        plugin.storage().addAudit(plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()),
                "STAFF_BROADCAST", CommandUtil.join(invocation.arguments(), 0));
    }
}
