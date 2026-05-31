package dev.staffrover.command;

import com.velocitypowered.api.command.SimpleCommand;

import java.util.concurrent.ThreadLocalRandom;

public final class StaffCoinflipCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (!CommandUtil.require(invocation.source(), "staffrover.recreation")) {
            return;
        }
        String result = ThreadLocalRandom.current().nextBoolean() ? "heads" : "tails";
        invocation.source().sendMessage(CommandUtil.success("Coinflip: " + result + "."));
    }
}
