package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.PlayerProfile;
import dev.staffrover.model.WatchEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public final class WatchCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public WatchCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.watch")) {
            return;
        }
        String[] args = invocation.arguments();
        if (args.length == 0) {
            invocation.source().sendMessage(CommandUtil.info("Active watches:"));
            for (WatchEntry watch : plugin.storage().activeWatches()) {
                invocation.source().sendMessage(Component.text("#" + watch.id() + " " + watch.targetName() + ": ", NamedTextColor.AQUA)
                        .append(Component.text(watch.reason(), NamedTextColor.GRAY)));
            }
            return;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                invocation.source().sendMessage(CommandUtil.error("Usage: /watch remove <player>"));
                return;
            }
            Optional<PlayerProfile> target = plugin.findProfile(args[1]);
            if (target.isEmpty()) {
                invocation.source().sendMessage(CommandUtil.error("Unknown player."));
                return;
            }
            plugin.storage().removeWatch(target.get().uuid())
                    .ifPresentOrElse(watch -> invocation.source().sendMessage(CommandUtil.success("Removed watch for " + target.get().lastName() + ".")),
                            () -> invocation.source().sendMessage(CommandUtil.error("No active watch found.")));
            return;
        }
        if (args.length < 2) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /watch <player> <reason> OR /watch remove <player>"));
            return;
        }
        Optional<PlayerProfile> target = plugin.findProfile(args[0]);
        if (target.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown player."));
            return;
        }
        WatchEntry watch = plugin.storage().addWatch(target.get().uuid(), target.get().lastName(),
                plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()), CommandUtil.join(args, 1));
        invocation.source().sendMessage(CommandUtil.success("Watch #" + watch.id() + " added for " + target.get().lastName() + "."));
    }
}
