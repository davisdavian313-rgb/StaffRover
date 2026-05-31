package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.staffrover.StaffRoverPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class VanishCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public VanishCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.vanish")) {
            return;
        }
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(CommandUtil.error("Only players can toggle vanish."));
            return;
        }
        boolean enabled = plugin.toggleVanish(player);
        player.sendMessage(enabled
                ? CommandUtil.success("Vanish enabled. Backend vanish support still needs a Paper companion or server vanish plugin.")
                : CommandUtil.info("Vanish disabled."));
        plugin.broadcastStaff(CommandUtil.prefix()
                .append(CommandUtil.name(player.getUsername()))
                .append(CommandUtil.detail(enabled ? " vanished." : " unvanished.")),
                "staffrover.vanish.notify");
    }
}
