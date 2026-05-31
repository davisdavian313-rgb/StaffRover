package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public final class StaffHelpCommand implements SimpleCommand {
    private static final List<String> COMMANDS = List.of(
            "/punish <player>",
            "/ban <player> <reason>, /tempban <player> <duration> <reason>",
            "/mute <player> <duration> <reason>, /warn <player> <reason>",
            "/banip <player> <reason>, /teamipban <player> <duration> <reason>",
            "/blacklist <player> <reason>",
            "/report <player> <reason>",
            "/reports, /reportclaim <id>, /reportclose <id> <reason>",
            "/evidence add <player> <url> [note], /evidence view <player>",
            "/whois <player>, /seen <player>",
            "/alts <player>, /evasion <player>",
            "/history <player>, /case <player>, /appealinfo <player>",
            "/appeal <punishmentId> <upheld|reduced|accepted|denied>",
            "/punishment revoke <id>, /unban <player>, /unmute <player>, /unwarn <player>",
            "/notes <player>, /note add <player> <note>",
            "/freeze <player>, /watch <player> <reason>",
            "/staffgui [panel] [player]",
            "/staffchat <message>, /staffbroadcast <message>",
            "/staffmode, /staffhub, /vanish, /stafflist, /staffstats",
            "/staffroll [max], /staffcoinflip, /auditlog"
    );

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.staff")) {
            return;
        }
        invocation.source().sendMessage(CommandUtil.info("STAFF commands:"));
        for (String command : COMMANDS) {
            invocation.source().sendMessage(Component.text(" - " + command, NamedTextColor.GRAY));
        }
    }
}
