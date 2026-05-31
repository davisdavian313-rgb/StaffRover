package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import dev.staffrover.model.Evidence;
import dev.staffrover.model.PlayerProfile;

import java.util.Optional;

public final class EvidenceCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public EvidenceCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length < 2) {
            invocation.source().sendMessage(CommandUtil.error("Usage: /evidence add <player> <url> [note] OR /evidence view <player>"));
            return;
        }
        if (args[0].equalsIgnoreCase("add")) {
            if (!CommandUtil.require(invocation.source(), "staffrover.evidence.add")) {
                return;
            }
            if (args.length < 3) {
                invocation.source().sendMessage(CommandUtil.error("Usage: /evidence add <player> <url> [note]"));
                return;
            }
            Optional<PlayerProfile> target = plugin.findProfile(args[1]);
            if (target.isEmpty()) {
                invocation.source().sendMessage(CommandUtil.error("Unknown player."));
                return;
            }
            Evidence item = plugin.storage().addEvidence(target.get().uuid(), target.get().lastName(), plugin.senderUuid(invocation.source()),
                    plugin.senderName(invocation.source()), args[2], args.length >= 4 ? CommandUtil.join(args, 3) : "", null, null);
            plugin.storage().addAudit(plugin.senderUuid(invocation.source()), plugin.senderName(invocation.source()),
                    "EVIDENCE_ADD", "Evidence #" + item.id() + " for " + target.get().lastName());
            invocation.source().sendMessage(CommandUtil.success("Evidence #" + item.id() + " added for " + target.get().lastName() + "."));
            return;
        }
        if (args[0].equalsIgnoreCase("view")) {
            if (!CommandUtil.require(invocation.source(), "staffrover.evidence.view")) {
                return;
            }
            Optional<PlayerProfile> target = plugin.findProfile(args[1]);
            if (target.isEmpty()) {
                invocation.source().sendMessage(CommandUtil.error("Unknown player."));
                return;
            }
            invocation.source().sendMessage(CommandUtil.info("Evidence for " + target.get().lastName() + ":"));
            for (Evidence item : plugin.storage().evidenceFor(target.get().uuid()).stream().limit(10).toList()) {
                invocation.source().sendMessage(CommandUtil.id(item.id())
                        .append(CommandUtil.detail(item.url() + " "))
                        .append(CommandUtil.detail(item.note())));
            }
            return;
        }
        invocation.source().sendMessage(CommandUtil.error("Unknown evidence subcommand."));
    }
}
