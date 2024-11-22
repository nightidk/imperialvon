package ru.nightidk.imperialvon.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.utils.ConfigUtils;

public class ConfigReload {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("iv")
                        .then(
                                Commands.literal("reload")
                                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                                        .executes(context -> {
                                            ConfigUtils.loadConfig(ImperialVon.configFile);
                                            context.getSource().sendSystemMessage(Component.literal("[ImperialVon] Config reloaded."));
                                            return 1;
                                        })
                        )
        );
    }
}