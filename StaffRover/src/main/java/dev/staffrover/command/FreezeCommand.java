package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.staffrover.StaffRoverPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class FreezeCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public FreezeCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.freeze")) {
            return;
        }
        if (invocation.arguments().length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /freeze <player>"));
            return;
        }
        Player target = plugin.server().getPlayer(invocation.arguments()[0]).orElse(null);
        if (target == null) {
            invocation.source().sendMessage(CommandUtil.error("That player is not online."));
            return;
        }
        boolean frozen = plugin.toggleFrozen(target);
        target.sendMessage(frozen
                ? Component.text("You have been frozen by staff. Do not log out.", NamedTextColor.RED)
                : Component.text("You have been unfrozen by staff.", NamedTextColor.GREEN));
        plugin.storage().addAudit(plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()),
                frozen ? "FREEZE" : "UNFREEZE", target.getUsername());
        plugin.broadcastStaff(CommandUtil.prefix()
                .append(CommandUtil.name(target.getUsername()))
                .append(CommandUtil.detail(frozen ? " was frozen by " : " was unfrozen by "))
                .append(CommandUtil.name(plugin.senderName(invocation.source()))),
                "staffrover.freeze.notify");
    }
}
