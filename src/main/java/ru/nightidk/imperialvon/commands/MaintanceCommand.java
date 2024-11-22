package ru.nightidk.imperialvon.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.configuration.ConfigVariables;
import ru.nightidk.imperialvon.utils.ChatMessageUtil;
import ru.nightidk.imperialvon.utils.ConfigUtils;
import ru.nightidk.imperialvon.utils.TextStyleUtil;
import ru.nightidk.jdautil.actions.MessageActions;
import ru.nightidk.jdautil.types.AuthorInfoBase;
import ru.nightidk.jdautil.types.BaseEmbed;

import java.awt.*;
import java.io.IOException;

import static ru.nightidk.imperialvon.utils.ChatMessageUtil.getStyledComponent;
import static ru.nightidk.imperialvon.utils.ChatMessageUtil.sendChatMessageToAll;

public class MaintanceCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("iv")
                        .then(
                                Commands.literal("maintains")
                                        .requires(s -> s.hasPermission(4))
                                        .then(
                                                Commands.literal("on")
                                                        .executes(context -> {
                                                            ConfigVariables.MAINTANCE = true;
                                                            try {
                                                                ConfigUtils.saveConfig(ImperialVon.configFile);
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                            ImperialVon.LOG.info("Maintains on.");
                                                            sendChatMessageToAll(
                                                                    context.getSource().getServer().getPlayerList().getPlayers(),
                                                                    getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                                                                            .append(getStyledComponent(" Включён режим технических работ.", TextStyleUtil.RED.getStyle())),
                                                                    ChatMessageUtil.MessageType.NOTIFY
                                                            );
                                                            context.getSource().getServer().getPlayerList().getPlayers().forEach(s -> {
                                                                if (!s.hasPermissions(4))
                                                                    s.connection.disconnect(getStyledComponent("На сервере ведутся технические работы.", TextStyleUtil.RED.getStyle()));
                                                            });

                                                            MessageActions.editMessage(
                                                                    "1250591961988206652",
                                                                    ConfigVariables.DISCORD_STATUS_MESSAGE,
                                                                    null,
                                                                    new BaseEmbed(
                                                                            "Информация об сервере",
                                                                            "Статус: Тех.работы",
                                                                            new Color(255, 136, 0).getRGB(),
                                                                            new AuthorInfoBase("Империя \"von\"")
                                                                    )
                                                            );
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                Commands.literal("off")
                                                        .executes(context -> {
                                                            ConfigVariables.MAINTANCE = false;
                                                            try {
                                                                ConfigUtils.saveConfig(ImperialVon.configFile);
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                            sendChatMessageToAll(
                                                                    context.getSource().getServer().getPlayerList().getPlayers(),
                                                                    getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                                                                            .append(getStyledComponent(" Отключён режим технических работ.", TextStyleUtil.RED.getStyle())),
                                                                    ChatMessageUtil.MessageType.NOTIFY
                                                            );
                                                            ImperialVon.LOG.info("Maintains off.");
                                                            MessageActions.editMessage(
                                                                    "1250591961988206652",
                                                                    ConfigVariables.DISCORD_STATUS_MESSAGE,
                                                                    null,
                                                                    new BaseEmbed(
                                                                            "Информация об сервере",
                                                                            "Статус: Работает",
                                                                            new Color(0, 255, 0).getRGB(),
                                                                            new AuthorInfoBase("Империя \"von\"")
                                                                    )
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )
        );
    }
}
