package net.cgerwyu.playerdeathbell.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.cgerwyu.playerdeathbell.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class PlayerDeathBellCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("playerdeathbell")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> showStatus(context.getSource()))
                        .then(Commands.literal("on")
                                .executes(context -> setEnabled(context.getSource(), true)))
                        .then(Commands.literal("off")
                                .executes(context -> setEnabled(context.getSource(), false)))
                        .then(Commands.literal("status")
                                .executes(context -> showStatus(context.getSource())))
        );
    }

    private static int setEnabled(CommandSourceStack source, boolean enabled) {
        Config.ENABLED.set(enabled);
        Config.SPEC.save();

        source.sendSuccess(
                () -> Component.literal("Player Death Bell is: " + (enabled ? "enabled" : "disabled")),
                true
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int showStatus(CommandSourceStack source) {
        boolean enabled = Config.ENABLED.get();

        source.sendSuccess(
                () -> Component.literal("Player Death Bell is: " + (enabled ? "enabled" : "disabled")),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private PlayerDeathBellCommand() {
    }
}
