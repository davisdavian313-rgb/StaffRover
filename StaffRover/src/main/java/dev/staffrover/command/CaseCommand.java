package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.Optional;

public final class CaseCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public CaseCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.case.view")) {
            return;
        }
        if (invocation.arguments().length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /case <player>"));
            return;
        }
        Optional<PlayerProfile> target = plugin.findProfile(invocation.arguments()[0]);
        if (target.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown player."));
            return;
        }
        PlayerProfile profile = target.get();
        invocation.source().sendMessage(CommandUtil.prefix()
                .append(CommandUtil.detail("Case summary: "))
                .append(CommandUtil.name(profile.lastName())));
        line(invocation, "Risk", plugin.evasionRisk(profile) + "/100");
        line(invocation, "Registered", CommandUtil.format(profile.registeredAt()));
        line(invocation, "Last seen", CommandUtil.ago(profile.lastLogoutAt()));
        line(invocation, "Punishments", Integer.toString(plugin.storage().punishmentsFor(profile.uuid()).size()));
        line(invocation, "Reports", Integer.toString(plugin.storage().reportsFor(profile.uuid()).size()));
        line(invocation, "Evidence", Integer.toString(plugin.storage().evidenceFor(profile.uuid()).size()));
        line(invocation, "Notes", Integer.toString(plugin.storage().notesFor(profile.uuid()).size()));
        line(invocation, "Linked accounts", Integer.toString(plugin.storage().linkedProfiles(profile).size()));
        invocation.source().sendMessage(CommandUtil.action("[History] ")
                .clickEvent(ClickEvent.runCommand("/history " + profile.lastName()))
                .append(CommandUtil.action("[Evidence] ")
                        .clickEvent(ClickEvent.runCommand("/evidence view " + profile.lastName())))
                .append(CommandUtil.action("[Notes] ")
                        .clickEvent(ClickEvent.runCommand("/notes " + profile.lastName())))
                .append(CommandUtil.danger("[Punish]")
                        .clickEvent(ClickEvent.runCommand("/punish " + profile.lastName()))));
    }

    private void line(Invocation invocation, String key, String value) {
        invocation.source().sendMessage(CommandUtil.label(key, value));
    }
}
