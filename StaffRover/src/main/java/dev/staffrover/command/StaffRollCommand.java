package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;

import java.util.concurrent.ThreadLocalRandom;

public final class StaffRollCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.recreation")) {
            return;
        }
        int max = 100;
        if (invocation.arguments().length >= 1) {
            try {
                max = Math.max(2, Integer.parseInt(invocation.arguments()[0]));
            } catch (NumberFormatException ignored) {
                invocation.source().sendMessage(CommandUtil.error("Usage: /staffroll [max]"));
                return;
            }
        }
        invocation.source().sendMessage(CommandUtil.success("You rolled " + ThreadLocalRandom.current().nextInt(1, max + 1) + " out of " + max + "."));
    }
}
