package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.PlayerProfile;

import java.util.Optional;

public final class SeenCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public SeenCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.lookup.seen")) {
            return;
        }
        String[] args = invocation.arguments();
        if (args.length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /seen <player>"));
            return;
        }
        Optional<PlayerProfile> profile = plugin.findProfile(args[0]);
        if (profile.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown player."));
            return;
        }
        if (plugin.server().getPlayer(profile.get().uuid()).isPresent()) {
            invocation.source().sendMessage(CommandUtil.success(profile.get().lastName() + " is online on " + profile.get().lastServer() + ". Registered: " + CommandUtil.format(profile.get().registeredAt())));
        } else {
            invocation.source().sendMessage(CommandUtil.info(profile.get().lastName() + " was last seen " + CommandUtil.ago(profile.get().lastLogoutAt()) + " on " + profile.get().lastServer() + ". Registered: " + CommandUtil.format(profile.get().registeredAt())));
        }
    }
}
