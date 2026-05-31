package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.staffrover.StaffRoverPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public final class StaffGuiCommand implements SimpleCommand {
    private final StaffRoverPlugin plugin;

    public StaffGuiCommand(StaffRoverPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.gui")) {
            return;
        }
        String[] args = invocation.arguments();
        String panel = args.length == 0 ? "main" : args[0].toLowerCase();
        String target = args.length >= 2 ? args[1] : "<player>";
        switch (panel) {
            case "punish" -> punish(invocation, target);
            case "reports" -> reports(invocation);
            case "lookup" -> lookup(invocation, target);
            case "tools" -> tools(invocation, target);
            case "fun", "recreation" -> recreation(invocation);
            default -> main(invocation, target);
        }
    }

    private void main(Invocation invocation, String target) {
        header(invocation, "Main Panel");
        button(invocation, "Punishments", "/staffgui punish " + target, "Open punishment shortcuts");
        button(invocation, "Reports", "/staffgui reports", "Open report queue actions");
        button(invocation, "Lookup", "/staffgui lookup " + target, "Open player lookup tools");
        button(invocation, "Tools", "/staffgui tools " + target, "Open staff tools");
        button(invocation, "Recreation", "/staffgui recreation", "Open staff recreation");
        invocation.source().sendMessage(CommandUtil.detail("Tip: /staffgui main <player> fills player actions."));
    }

    private void punish(Invocation invocation, String target) {
        header(invocation, "Punishments");
        button(invocation, "Punish Menu", "/punish " + target, "Open section/offense punishment menu");
        button(invocation, "Warn", "/warn " + target + " ", "Warn this player");
        button(invocation, "Mute", "/mute " + target + " 30m ", "Mute this player");
        button(invocation, "Tempban", "/tempban " + target + " 7d ", "Tempban this player");
        button(invocation, "Ban", "/ban " + target + " ", "Ban this player");
        button(invocation, "Blacklist", "/blacklist " + target + " ", "Blacklist from your current server");
        back(invocation, target);
    }

    private void reports(Invocation invocation) {
        header(invocation, "Reports");
        button(invocation, "Open Reports", "/reports", "Show open reports");
        button(invocation, "Staff Stats", "/staffstats", "Show your staff stats");
        button(invocation, "Audit Log", "/auditlog", "Show recent STAFF audit entries");
        back(invocation, "<player>");
    }

    private void lookup(Invocation invocation, String target) {
        header(invocation, "Lookup");
        button(invocation, "Whois", "/whois " + target, "Open full profile");
        button(invocation, "Seen", "/seen " + target, "Check last seen");
        button(invocation, "Alts", "/alts " + target, "Check linked accounts");
        button(invocation, "Evasion", "/evasion " + target, "Check evasion risk");
        button(invocation, "Case", "/case " + target, "Open case summary");
        back(invocation, target);
    }

    private void tools(Invocation invocation, String target) {
        header(invocation, "Tools");
        button(invocation, "Freeze", "/freeze " + target, "Toggle freeze");
        button(invocation, "Watch", "/watch " + target + " ", "Add player watch");
        button(invocation, "Evidence", "/evidence view " + target, "View evidence");
        button(invocation, "Notes", "/notes " + target, "View staff notes");
        button(invocation, "Staff Hub", "/staffhub", "Go to staff hub");
        button(invocation, "Vanish", "/vanish", "Toggle vanish");
        button(invocation, "Staff Mode", "/staffmode", "Toggle staff mode");
        back(invocation, target);
    }

    private void recreation(Invocation invocation) {
        header(invocation, "Recreation");
        button(invocation, "Roll", "/staffroll", "Roll 1-100");
        button(invocation, "Coinflip", "/staffcoinflip", "Flip a coin");
        button(invocation, "Staff List", "/stafflist", "Show online staff");
        back(invocation, "<player>");
    }

    private void header(Invocation invocation, String title) {
        invocation.source().sendMessage(Component.text(" "));
        invocation.source().sendMessage(CommandUtil.prefix()
                .append(CommandUtil.name(title))
                .append(CommandUtil.detail(" - clickable GUI")));
    }

    private void button(Invocation invocation, String label, String command, String hover) {
        invocation.source().sendMessage(CommandUtil.bullet()
                .append(CommandUtil.action("[" + label + "]")
                        .clickEvent(ClickEvent.suggestCommand(command))
                        .hoverEvent(HoverEvent.showText(CommandUtil.detail(hover)))));
    }

    private void back(Invocation invocation, String target) {
        invocation.source().sendMessage(CommandUtil.bullet()
                .append(CommandUtil.detail("[Back]")
                        .clickEvent(ClickEvent.runCommand("/staffgui main " + target))
                        .hoverEvent(HoverEvent.showText(CommandUtil.detail("Return to the main panel")))));
    }
}
