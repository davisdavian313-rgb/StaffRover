package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.staffrover.StaffRoverPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class StaffChatCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public StaffChatCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.staffchat")) {
            return;
        }
        if (invocation.arguments().length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /staffchat <message>"));
            return;
        }
        String message = CommandUtil.join(invocation.arguments(), 0);
        Component line = Component.text("")
                .append(CommandUtil.tag("SC", CommandUtil.BRAND))
                .append(Component.space())
                .append(prefix(invocation))
                .append(senderTag(invocation))
                .append(Component.space())
                .append(stateTags(invocation))
                .append(Component.text(" > ", CommandUtil.PANEL))
                .append(ChatText.colored(message))
                .hoverEvent(HoverEvent.showText(hover(invocation)))
                .clickEvent(ClickEvent.suggestCommand("/staffchat "));
        plugin.broadcastStaff(line, "staffrover.staffchat");
    }

    private Component prefix(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            return Component.empty();
        }
        String prefix = plugin.luckPermsPrefix(player);
        if (prefix.isBlank()) {
            return Component.empty();
        }
        return ChatText.colored(prefix).append(Component.space());
    }

    private Component senderTag(Invocation invocation) {
        TextColor nameColor = invocation.source() instanceof Player ? CommandUtil.ACCENT : TextColor.color(0xD6B4FF);
        return Component.text(plugin.senderName(invocation.source()), nameColor, TextDecoration.BOLD);
    }

    private Component stateTags(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            return CommandUtil.tag("CONSOLE", TextColor.color(0xD6B4FF));
        }
        Component tags = Component.empty();
        String serverName = player.getCurrentServer()
                .map(server -> server.getServerInfo().getName())
                .orElse("limbo");
        tags = tags.append(CommandUtil.tag(serverName, TextColor.color(0x7CFFB2)));
        if (plugin.staffMode(player)) {
            tags = tags.append(Component.space()).append(CommandUtil.tag("MODE", CommandUtil.ACCENT));
        }
        if (plugin.vanished(player)) {
            tags = tags.append(Component.space()).append(CommandUtil.tag("V", TextColor.color(0xD6B4FF)));
        }
        if (plugin.frozen(player)) {
            tags = tags.append(Component.space()).append(CommandUtil.tag("FROZEN", CommandUtil.ERROR));
        }
        return tags;
    }

    private Component hover(Invocation invocation) {
        Component hover = Component.text("STAFF Chat", CommandUtil.BRAND, TextDecoration.BOLD)
                .append(Component.newline())
                .append(CommandUtil.label("Sender", plugin.senderName(invocation.source())));
        if (invocation.source() instanceof Player player) {
            hover = hover.append(Component.newline())
                    .append(CommandUtil.label("Server", player.getCurrentServer()
                            .map(server -> server.getServerInfo().getName())
                            .orElse("limbo")))
                    .append(Component.newline())
                    .append(CommandUtil.label("Staff mode", plugin.staffMode(player) ? "enabled" : "disabled"))
                    .append(Component.newline())
                    .append(CommandUtil.label("Vanish", plugin.vanished(player) ? "enabled" : "disabled"));
        }
        return hover.append(Component.newline())
                .append(Component.text("Click to reply in staff chat.", NamedTextColor.GRAY));
    }
}
