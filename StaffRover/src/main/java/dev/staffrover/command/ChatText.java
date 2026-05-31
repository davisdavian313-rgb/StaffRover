package dev.staffrover.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

final class ChatText {
    private ChatText() {
    }

    static Component colored(String input) {
        if (input == null || input.isBlank()) {
            return Component.empty();
        }
        String trimmed = input.trim();
        if (trimmed.startsWith("<gradient:")) {
            int close = trimmed.indexOf('>');
            int end = trimmed.lastIndexOf("</gradient>");
            if (close > 10 && end > close) {
                String[] colors = trimmed.substring(10, close).split(":");
                if (colors.length >= 2) {
                    TextColor start = color(colors[0]);
                    TextColor finish = color(colors[1]);
                    if (start != null && finish != null) {
                        return gradient(trimmed.substring(close + 1, end), start, finish);
                    }
                }
            }
        }
        return legacy(input.replace('§', '&'));
    }

    private static Component legacy(String input) {
        Component component = Component.empty();
        TextColor current = CommandUtil.TEXT;
        StringBuilder buffer = new StringBuilder();
        for (int index = 0; index < input.length(); index++) {
            char character = input.charAt(index);
            if (character == '&' && index + 1 < input.length()) {
                TextColor next = null;
                char code = Character.toLowerCase(input.charAt(index + 1));
                if (code == '#' && index + 7 < input.length()) {
                    next = color(input.substring(index + 2, index + 8));
                    if (next != null) {
                        index += 7;
                    }
                } else {
                    next = legacyColor(code);
                    if (next != null) {
                        index++;
                    }
                }
                if (next != null) {
                    component = append(component, buffer, current);
                    buffer.setLength(0);
                    current = next;
                    continue;
                }
            }
            buffer.append(character);
        }
        return append(component, buffer, current);
    }

    private static Component append(Component component, StringBuilder buffer, TextColor color) {
        if (buffer.isEmpty()) {
            return component;
        }
        return component.append(Component.text(buffer.toString(), color));
    }

    private static TextColor legacyColor(char code) {
        return switch (code) {
            case '0' -> NamedTextColor.BLACK;
            case '1' -> NamedTextColor.DARK_BLUE;
            case '2' -> NamedTextColor.DARK_GREEN;
            case '3' -> NamedTextColor.DARK_AQUA;
            case '4' -> NamedTextColor.DARK_RED;
            case '5' -> NamedTextColor.DARK_PURPLE;
            case '6' -> NamedTextColor.GOLD;
            case '7' -> NamedTextColor.GRAY;
            case '8' -> NamedTextColor.DARK_GRAY;
            case '9' -> NamedTextColor.BLUE;
            case 'a' -> NamedTextColor.GREEN;
            case 'b' -> NamedTextColor.AQUA;
            case 'c' -> NamedTextColor.RED;
            case 'd' -> NamedTextColor.LIGHT_PURPLE;
            case 'e' -> NamedTextColor.YELLOW;
            case 'f' -> NamedTextColor.WHITE;
            case 'r' -> CommandUtil.TEXT;
            default -> null;
        };
    }

    private static Component gradient(String text, TextColor start, TextColor finish) {
        if (text.isEmpty()) {
            return Component.empty();
        }
        Component component = Component.empty();
        int length = Math.max(text.length() - 1, 1);
        for (int index = 0; index < text.length(); index++) {
            double ratio = index / (double) length;
            int red = mix(start.red(), finish.red(), ratio);
            int green = mix(start.green(), finish.green(), ratio);
            int blue = mix(start.blue(), finish.blue(), ratio);
            component = component.append(Component.text(String.valueOf(text.charAt(index)), TextColor.color(red, green, blue)));
        }
        return component;
    }

    private static int mix(int start, int finish, double ratio) {
        return (int) Math.round(start + ((finish - start) * ratio));
    }

    private static TextColor color(String value) {
        String cleaned = value.trim();
        if (cleaned.startsWith("#")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.length() != 6) {
            return null;
        }
        try {
            return TextColor.color(Integer.parseInt(cleaned, 16));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
