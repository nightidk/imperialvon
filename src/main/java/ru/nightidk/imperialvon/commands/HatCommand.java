package ru.nightidk.imperialvon.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.utils.ConfigUtils;
import ru.nightidk.imperialvon.utils.TextStyleUtil;

import static ru.nightidk.imperialvon.utils.ChatMessageUtil.getStyledComponent;
import static ru.nightidk.imperialvon.utils.ChatMessageUtil.sendChatMessageToPlayer;

public class HatCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("hat")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return -1;

                            ItemStack itemInHand = player.getMainHandItem();

                            if (itemInHand.isEmpty()) {
                                sendChatMessageToPlayer(player,
                                        getStyledComponent("[IVHat] ", TextStyleUtil.DARK_AQUA.getStyle())
                                                .append(getStyledComponent("У вас пустая рука, нечего надевать!", TextStyleUtil.RED.getStyle()))
                                );
                                return -1;
                            }

                            ItemStack currentHelmet = player.getInventory().armor.get(3);

                            player.getInventory().armor.set(3, itemInHand);
                            player.getInventory().setItem(player.getInventory().selected, currentHelmet);

                            sendChatMessageToPlayer(player,
                                    getStyledComponent("[IVHat] ", TextStyleUtil.DARK_AQUA.getStyle())
                                            .append(getStyledComponent("Вы надели предмет на голову!", TextStyleUtil.GREEN.getStyle()))
                            );

                            return 1;
                        })
        );
    }
}
