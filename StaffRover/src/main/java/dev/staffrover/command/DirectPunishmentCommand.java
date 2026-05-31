package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.PlayerProfile;
import dev.staffrover.model.Punishment;
import dev.staffrover.model.ServerBlacklist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public final class DirectPunishmentCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;
    private final String commandName;
    private final String type;
    private final boolean needsDuration;
    private final boolean serverScoped;

    public DirectPunishmentCommand(StaffRoverPlugin plugin, String commandName, String type,
                                   boolean needsDuration, boolean serverScoped) {
        this.plugin = plugin;
        this.commandName = commandName;
        this.type = type;
        this.needsDuration = needsDuration;
        this.serverScoped = serverScoped;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.punish." + commandName)) {
            return;
        }
        String[] args = invocation.arguments();
        int minimum = needsDuration ? 3 : 2;
        if (args.length < minimum) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /" + commandName + " <player> "
                    + (needsDuration ? "<duration> " : "") + "<reason>"));
            return;
        }
        Optional<PlayerProfile> target = plugin.findProfile(args[0]);
        if (target.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown player."));
            return;
        }
        String duration = needsDuration ? args[1] : type.equals("TEMPBAN") ? "30d" : "permanent";
        String reason = CommandUtil.join(args, needsDuration ? 2 : 1);
        if (serverScoped) {
            blacklistCurrentServer(invocation, target.get(), reason);
            return;
        }
        Punishment punishment = plugin.storage().addPunishment(target.get().uuid(), target.get().lastName(),
                plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()), "manual",
                commandName, 1, type, duration, reason, null);
        plugin.applyPunishmentAction(punishment);
        plugin.storage().addAudit(plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()),
                type, target.get().lastName() + " #" + punishment.id() + " " + reason);
        invocation.source().sendMessage(CommandUtil.success(type + " #" + punishment.id() + " issued for " + target.get().lastName() + "."));
        plugin.broadcastStaff(CommandUtil.prefix()
                        .append(CommandUtil.name(target.get().lastName()))
                        .append(CommandUtil.detail(" received "))
                        .append(CommandUtil.danger(type))
                        .append(CommandUtil.detail(" by "))
                        .append(CommandUtil.name(plugin.senderName(invocation.source())))
                        .append(CommandUtil.detail(": " + reason)),
                "staffrover.punish.notify");
    }

    private void blacklistCurrentServer(Invocation invocation, PlayerProfile target, String reason) {
        if (!(invocation.source() instanceof Player staff)) {
            invocation.source().sendMessage(CommandUtil.error("/blacklist must be used by an online staff member so STAFF knows the current server."));
            return;
        }
        String serverName = staff.getCurrentServer()
                .map(server -> server.getServerInfo().getName())
                .orElse(null);
        if (serverName == null) {
            invocation.source().sendMessage(CommandUtil.error("You are not connected to a server."));
            return;
        }
        ServerBlacklist blacklist = plugin.storage().addServerBlacklist(target.uuid(), target.lastName(), serverName,
                plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()), reason);
        Punishment punishment = plugin.storage().addPunishment(target.uuid(), target.lastName(),
                plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()), "server-blacklist",
                serverName, 1, "BLACKLIST", "server", reason, null);
        plugin.server().getPlayer(target.uuid()).ifPresent(player -> player.getCurrentServer().ifPresent(connection -> {
            if (connection.getServerInfo().getName().equalsIgnoreCase(serverName)) {
                player.disconnect(Component.text("You are blacklisted from " + serverName + ": " + reason, NamedTextColor.RED));
            }
        }));
        plugin.storage().addAudit(plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()),
                "BLACKLIST", target.lastName() + " from " + serverName + " #" + blacklist.id());
        invocation.source().sendMessage(CommandUtil.success("Blacklisted " + target.lastName() + " from " + serverName + " as punishment #" + punishment.id() + "."));
    }
}
