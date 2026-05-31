package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.staffrover.StaffRoverPlugin;

import java.util.Optional;

public final class StaffHubCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public StaffHubCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.staffhub")) {
            return;
        }
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(CommandUtil.error("Only players can use /staffhub."));
            return;
        }
        Optional<RegisteredServer> server = plugin.server().getServer("staffhub");
        if (server.isEmpty()) {
            server = plugin.server().getServer("hub");
        }
        if (server.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("No staffhub or hub server is registered on the proxy."));
            return;
        }
        player.createConnectionRequest(server.get()).fireAndForget();
        invocation.source().sendMessage(CommandUtil.success("Sending you to " + server.get().getServerInfo().getName() + "."));
    }
}
