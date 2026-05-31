package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.PlayerProfile;
import dev.staffrover.model.StaffNote;

import java.util.Optional;

public final class NotesCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public NotesCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length < 1) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /notes <player> OR /note add <player> <note>"));
            return;
        }
        if (args[0].equalsIgnoreCase("add")) {
            if (!CommandUtil.require(invocation.source(), "staffrover.notes.add")) {
                return;
            }
            if (args.length < 3) {
                invocation.source().sendMessage(CommandUtil.error("Usage: /note add <player> <note>"));
                return;
            }
            Optional<PlayerProfile> target = plugin.findProfile(args[1]);
            if (target.isEmpty()) {
                invocation.source().sendMessage(CommandUtil.error("Unknown player."));
                return;
            }
            StaffNote note = plugin.storage().addNote(target.get().uuid(), target.get().lastName(),
                    plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()), CommandUtil.join(args, 2));
            plugin.storage().addAudit(plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()),
                    "NOTE_ADD", "Added note #" + note.id() + " for " + target.get().lastName());
            invocation.source().sendMessage(CommandUtil.success("Note #" + note.id() + " added."));
            return;
        }
        if (!CommandUtil.require(invocation.source(), "staffrover.notes.view")) {
            return;
        }
        Optional<PlayerProfile> target = plugin.findProfile(args[0]);
        if (target.isEmpty()) {
            invocation.source().sendMessage(CommandUtil.error("Unknown player."));
            return;
        }
        invocation.source().sendMessage(CommandUtil.info("Notes for " + target.get().lastName() + ":"));
        for (StaffNote note : plugin.storage().notesFor(target.get().uuid()).stream().limit(10).toList()) {
            invocation.source().sendMessage(CommandUtil.id(note.id())
                    .append(CommandUtil.name(note.staffName()))
                    .append(CommandUtil.detail(": " + note.note())));
        }
    }
}
