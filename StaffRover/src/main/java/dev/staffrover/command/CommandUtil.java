package dev.staffrover.command;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class CommandUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")
            .withZone(ZoneId.systemDefault());
    public static final TextColor BRAND = TextColor.color(0x31D7FF);
    public static final TextColor ACCENT = TextColor.color(0xFFD166);
    public static final TextColor SUCCESS = TextColor.color(0x7CFFB2);
    public static final TextColor ERROR = TextColor.color(0xFF5C7A);
    public static final TextColor MUTED = TextColor.color(0xA7B0C0);
    public static final TextColor TEXT = TextColor.color(0xF3F6FF);
    public static final TextColor PANEL = TextColor.color(0x3B4454);

    private CommandUtil() {
    }

    static boolean require(CommandSource source, String permission) {
        if (source.hasPermission(permission) || source.hasPermission("staffrover.admin")) {
            return true;
        }
        source.sendMessage(error("You do not have permission for that."));
        return false;
    }

    public static Component prefix() {
        return Component.text("[", PANEL)
                .append(Component.text("STAFF", BRAND, TextDecoration.BOLD))
                .append(Component.text("] ", PANEL));
    }

    static Component info(String message) {
        return prefix().append(Component.text(message, MUTED));
    }

    static Component success(String message) {
        return prefix().append(Component.text(message, SUCCESS));
    }

    public static Component error(String message) {
        return prefix().append(Component.text(message, ERROR));
    }

    static Component label(String label, String value) {
        return Component.text(label + ": ", BRAND)
                .append(Component.text(value, TEXT));
    }

    static Component id(int id) {
        return Component.text("#" + id + " ", BRAND);
    }

    public static Component name(String value) {
        return Component.text(value, ACCENT, TextDecoration.BOLD);
    }

    public static Component detail(String value) {
        return Component.text(value, MUTED);
    }

    public static Component action(String value) {
        return Component.text(value, SUCCESS, TextDecoration.BOLD);
    }

    public static Component danger(String value) {
        return Component.text(value, ERROR, TextDecoration.BOLD);
    }

    static Component bullet() {
        return Component.text(" - ", PANEL);
    }

    static Component tag(String value, TextColor color) {
        return Component.text("[", PANEL)
                .append(Component.text(value, color, TextDecoration.BOLD))
                .append(Component.text("]", PANEL));
    }

    static String join(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int index = start; index < args.length; index++) {
            if (index > start) {
                builder.append(' ');
            }
            builder.append(args[index]);
        }
        return builder.toString();
    }

    static String format(Instant instant) {
        return instant == null ? "never" : FORMATTER.format(instant);
    }

    static String ago(Instant instant) {
        if (instant == null) {
            return "never";
        }
        Duration duration = Duration.between(instant, Instant.now());
        long days = duration.toDays();
        if (days > 0) {
            return days + "d ago";
        }
        long hours = duration.toHours();
        if (hours > 0) {
            return hours + "h ago";
        }
        long minutes = duration.toMinutes();
        return Math.max(minutes, 0) + "m ago";
    }
}
