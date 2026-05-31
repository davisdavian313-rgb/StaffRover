package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.PlayerProfile;
import dev.staffrover.model.Punishment;

import java.util.Optional;

public final class HistoryCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public HistoryCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.history.view")) {
            return;
        }
        String[] args = invocation.arguments();
        if (args.length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /history <player>"));
            return;
        }
        Optional<PlayerProfile> profile = plugin.findProfile(args[0]);
        if (profile.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown player."));
            return;
        }
        invocation.source().sendMessage(CommandUtil.info("Punishment history for " + profile.get().lastName() + ":"));
        for (Punishment punishment : plugin.storage().punishmentsFor(profile.get().uuid()).stream().limit(10).toList()) {
            invocation.source().sendMessage(CommandUtil.id(punishment.id())
                    .append(CommandUtil.name(punishment.type() + " " + punishment.duration()))
                    .append(CommandUtil.detail(" " + punishment.reason() + " by "))
                    .append(CommandUtil.name(punishment.staffName()))
                    .append(CommandUtil.detail(" | " + punishment.status() + " | appeal " + punishment.appealStatus())));
        }
    }
}
