package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.PlayerProfile;

import java.util.Optional;

public final class WhoisCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public WhoisCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.lookup.whois")) {
            return;
        }
        String[] args = invocation.arguments();
        if (args.length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /whois <player>"));
            return;
        }
        Optional<PlayerProfile> profile = plugin.findProfile(args[0]);
        if (profile.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown player."));
            return;
        }
        PlayerProfile target = profile.get();
        Optional<Player> online = plugin.server().getPlayer(target.uuid());
        invocation.source().sendMessage(CommandUtil.prefix()
                .append(CommandUtil.detail("Player info: "))
                .append(CommandUtil.name(target.lastName())));
        line(invocation, "UUID", target.uuid().toString());
        line(invocation, "Registered", CommandUtil.format(target.registeredAt()));
        line(invocation, "Last login", CommandUtil.format(target.lastLoginAt()));
        line(invocation, "Last logout", CommandUtil.format(target.lastLogoutAt()));
        line(invocation, "Online", online.isPresent() ? "Yes" : "No");
        line(invocation, "Current server", online.flatMap(Player::getCurrentServer)
                .map(server -> server.getServerInfo().getName()).orElse(target.lastServer()));
        line(invocation, "Known accounts", Integer.toString(plugin.storage().linkedProfiles(target).size()));
        line(invocation, "Total punishments", Integer.toString(plugin.storage().punishmentsFor(target.uuid()).size()));
        line(invocation, "Reports against", Integer.toString(plugin.storage().reportsFor(target.uuid()).size()));
        line(invocation, "Evidence items", Integer.toString(plugin.storage().evidenceFor(target.uuid()).size()));
        if (invocation.source().hasPermission("staffrover.lookup.ip") || invocation.source().hasPermission("staffrover.admin")) {
            line(invocation, "First IP", target.firstIp());
            line(invocation, "Last IP", target.lastIp());
        }
    }

    private void line(Invocation invocation, String key, String value) {
        invocation.source().sendMessage(CommandUtil.label(key, value));
    }
}
