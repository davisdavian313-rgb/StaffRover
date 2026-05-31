package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.PlayerProfile;

import java.util.Optional;

public final class AltsCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public AltsCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.alts.view")) {
            return;
        }
        String[] args = invocation.arguments();
        if (args.length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /alts <player>"));
            return;
        }
        Optional<PlayerProfile> profile = plugin.findProfile(args[0]);
        if (profile.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown player."));
            return;
        }
        invocation.source().sendMessage(CommandUtil.info("Known linked accounts for " + profile.get().lastName() + ":"));
        for (PlayerProfile linked : plugin.storage().linkedProfiles(profile.get())) {
            invocation.source().sendMessage(CommandUtil.bullet()
                    .append(CommandUtil.name(linked.lastName()))
                    .append(CommandUtil.detail(" last seen " + CommandUtil.ago(linked.lastLogoutAt()))));
        }
    }
}
