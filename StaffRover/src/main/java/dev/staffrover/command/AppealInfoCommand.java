package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.Evidence;
import dev.staffrover.model.PlayerProfile;
import dev.staffrover.model.Punishment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public final class AppealInfoCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public AppealInfoCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.appeal.view")) {
            return;
        }
        if (invocation.arguments().length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /appealinfo <player>"));
            return;
        }
        Optional<PlayerProfile> target = plugin.findProfile(invocation.arguments()[0]);
        if (target.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown player."));
            return;
        }
        PlayerProfile profile = target.get();
        invocation.source().sendMessage(CommandUtil.prefix().append(Component.text("Appeal info for " + profile.lastName(), NamedTextColor.YELLOW)));
        for (Punishment punishment : plugin.storage().punishmentsFor(profile.uuid()).stream().limit(5).toList()) {
            invocation.source().sendMessage(Component.text("#" + punishment.id() + " ", NamedTextColor.AQUA)
                    .append(Component.text(punishment.type() + " " + punishment.duration() + " ", NamedTextColor.YELLOW))
                    .append(Component.text(punishment.reason() + " | " + CommandUtil.format(punishment.createdAt())
                            + " | status " + punishment.status() + " | appeal " + punishment.appealStatus(), NamedTextColor.GRAY)));
            if (punishment.evidenceId() != null) {
                plugin.storage().evidenceById(punishment.evidenceId()).ifPresent(evidence -> evidenceLine(invocation, evidence));
            }
        }
    }

    private void evidenceLine(Invocation invocation, Evidence evidence) {
        invocation.source().sendMessage(Component.text("  Evidence #" + evidence.id() + ": ", NamedTextColor.DARK_AQUA)
                .append(Component.text(evidence.url(), NamedTextColor.GRAY)));
    }
}
