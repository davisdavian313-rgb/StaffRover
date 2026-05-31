package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.stream.Collectors;

public final class StaffListCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public StaffListCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.stafflist")) {
            return;
        }
        String staff = plugin.server().getAllPlayers().stream()
                .filter(player -> player.hasPermission("staffrover.staff"))
                .map(player -> player.getUsername()
                        + (plugin.staffMode(player) ? " [staffmode]" : "")
                        + (plugin.vanished(player) ? " [vanished]" : ""))
                .collect(Collectors.joining(", "));
        invocation.source().sendMessage(Component.text("Online staff: ", NamedTextColor.AQUA)
                .append(Component.text(staff.isBlank() ? "none" : staff, NamedTextColor.GRAY)));
    }
}
