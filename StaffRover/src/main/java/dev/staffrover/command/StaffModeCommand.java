package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.staffrover.StaffRoverPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class StaffModeCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public StaffModeCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.staffmode")) {
            return;
        }
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(CommandUtil.error("Only players can toggle staff mode."));
            return;
        }
        boolean enabled = plugin.toggleStaffMode(player);
        player.sendMessage(enabled
                ? CommandUtil.success("Staff mode enabled.")
                : CommandUtil.info("Staff mode disabled."));
        plugin.broadcastStaff(CommandUtil.prefix()
                .append(CommandUtil.name(player.getUsername()))
                .append(CommandUtil.detail(enabled ? " entered staff mode." : " left staff mode.")),
                "staffrover.staffmode.notify");
    }
}
