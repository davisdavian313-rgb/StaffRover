package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.PlayerProfile;
import dev.staffrover.model.Punishment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Optional;

public final class EvasionCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public EvasionCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.evasion.view")) {
            return;
        }
        String[] args = invocation.arguments();
        if (args.length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /evasion <player>"));
            return;
        }
        Optional<PlayerProfile> profile = plugin.findProfile(args[0]);
        if (profile.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown player."));
            return;
        }
        PlayerProfile target = profile.get();
        int risk = plugin.evasionRisk(target);
        invocation.source().sendMessage(CommandUtil.prefix()
                .append(Component.text("Ban evasion check for ", NamedTextColor.GRAY))
                .append(Component.text(target.lastName(), NamedTextColor.YELLOW))
                .append(Component.text(" risk " + risk + "/100", risk >= 75 ? NamedTextColor.RED : NamedTextColor.GOLD)));
        for (PlayerProfile linked : plugin.storage().linkedProfiles(target)) {
            List<Punishment> punishments = plugin.storage().punishmentsFor(linked.uuid());
            long bans = punishments.stream().filter(punishment -> punishment.type().contains("BAN")).count();
            invocation.source().sendMessage(Component.text(" - " + linked.lastName(), NamedTextColor.YELLOW)
                    .append(Component.text(" shared IP, bans/tempbans: " + bans + ", last seen " + CommandUtil.ago(linked.lastLogoutAt()), NamedTextColor.GRAY)));
        }
    }
}
