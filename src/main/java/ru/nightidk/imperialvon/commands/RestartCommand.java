package ru.nightidk.imperialvon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.configuration.ConfigVariables;
import ru.nightidk.imperialvon.listeners.serverside.ModEventListener;
import ru.nightidk.imperialvon.utils.ChatMessageUtil;
import ru.nightidk.imperialvon.utils.ConfigUtils;
import ru.nightidk.imperialvon.utils.TextStyleUtil;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.nightidk.imperialvon.utils.ChatMessageUtil.*;

public class RestartCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("iv")
                        .then(Commands.literal("auto")
                                .requires(r -> r.hasPermission(4))
                                .executes(context -> {
                                    if (context.getSource().getPlayer() == null) return -1;
                                    ConfigVariables.AUTORESTART = !ConfigVariables.AUTORESTART;
                                    try {
                                        ConfigUtils.saveConfig(ImperialVon.configFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    if (ConfigVariables.AUTORESTART) {
                                        ModEventListener.setTickRestart(ConfigVariables.RESTART_TIME);
                                        sendChatMessageToAll(context.getSource().getServer().getPlayerList().getPlayers(),
                                                getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                                                        .append(getStyledComponent("Автоматические перезагрузки включены.", TextStyleUtil.WHITE.getStyle())),
                                                MessageType.NOTIFY
                                        );
                                    } else {
                                        ModEventListener.setTickRestart(-1);
                                        sendChatMessageToAll(context.getSource().getServer().getPlayerList().getPlayers(),
                                                getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                                                        .append(getStyledComponent("Автоматические перезагрузки отключены.", TextStyleUtil.WHITE.getStyle())),
                                                MessageType.NOTIFY
                                        );
                                    }
                                    return 1;
                                })
                        )
                        .then(Commands.literal("cancelRestart")
                                .requires(r -> r.hasPermission(4))
                                .executes(context -> {
                                    if (context.getSource().getPlayer() == null) return -1;
                                    sendChatMessageToPlayer(context.getSource().getPlayer(), "[ImperialVon] Restart canceled");
                                    ModEventListener.setTickRestart(ModEventListener.tickForPlannedRestart);
                                    sendChatMessageToAll(context.getSource().getServer().getPlayerList().getPlayers(),
                                            getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                                                    .append(getStyledComponent(" Перезагрузка, назначенная администратором, отменена. Следующая плановая перезагрузка через %s.".formatted(fromTicksToStringTime(ModEventListener.tickForPlannedRestart)), TextStyleUtil.WHITE.getStyle())),
                                            MessageType.NOTIFY
                                    );
                                    ModEventListener.tickForPlannedRestart = 0;
                                    return 1;
                                })
                        )
                        .then(Commands.literal("instantRestart")
                                .requires(r -> r.hasPermission(4))
                                .executes(context -> {
                                    if (context.getSource().getPlayer() == null) return -1;
                                    sendChatMessageToPlayer(context.getSource().getPlayer(), "[ImperialVon] Restarting server...");
                                    sendChatMessageToAll(
                                            context.getSource().getServer().getPlayerList().getPlayers(),
                                            getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                                                    .append(getStyledComponent(" Перезагрузка сервера...", TextStyleUtil.WHITE.getStyle())),
                                            ChatMessageUtil.MessageType.NOTIFY
                                    );
                                    context.getSource().getServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.connection.disconnect(getStyledComponent("Перезагрузка сервера...", TextStyleUtil.DARK_AQUA.getStyle())));
                                    context.getSource().getServer().stopServer();
                                    return 1;
                                })
                        )
                        .then(Commands.literal("restart")
                                .requires(r -> r.hasPermission(4))
                                .then(Commands.argument("time", StringArgumentType.string())
                                        .executes(context -> {
                                            String time = context.getArgument("time", String.class);
                                            int ticks = getTimeFromString(time);
                                            if (context.getSource().getPlayer() == null) return -1;
                                            if (ticks == -1)
                                                sendChatMessageToPlayer(context.getSource().getPlayer(), getStyledComponent("[ImperialVon] Something goes wrong.", TextStyleUtil.RED.getStyle()));
                                            else {
                                                sendChatMessageToPlayer(context.getSource().getPlayer(), "[ImperialVon] Restarting server planned in %s.".formatted(fromTicksToStringTime(ticks)));
                                                sendChatMessageToAll(context.getSource().getServer().getPlayerList().getPlayers(),
                                                        getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                                                            .append(getStyledComponent(" Администратором назначена перезагрузка сервера через %s.".formatted(fromTicksToStringTime(ticks)), TextStyleUtil.WHITE.getStyle())),
                                                        MessageType.NOTIFY
                                                );
                                                ModEventListener.tickForPlannedRestart = ModEventListener.getTickRestart();
                                                ModEventListener.setTickRestart(ticks);
                                            }
                                            return 1;
                                        })
                                )
                        )
        );
    }

    protected static int getTimeFromString(String time) {
        String regex = "(?:(?<hours>\\d+)h)?(?:(?<minutes>\\d+)m)?(?:(?<seconds>\\d+)s)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(time);

        if (matcher.matches()) {
            int hours = 0;
            int minutes = 0;
            int seconds = 0;
            if (matcher.group("hours") != null)
                 hours = Integer.parseInt(matcher.group("hours"));
            if (matcher.group("minutes") != null)
                minutes = Integer.parseInt(matcher.group("minutes"));
            if (matcher.group("seconds") != null)
                seconds = Integer.parseInt(matcher.group("seconds"));

            int ticks = 0;

            ticks += hours * 60 * 60 * 20;
            ticks += minutes * 60 * 20;
            ticks += seconds * 20;

            return ticks;
        } else {
            return -1;
        }
    }

    protected static String fromTicksToStringTime(int ticks) {
        int hours = ticks / (60 * 60 * 20);
        ticks -= hours * 60 * 60 * 20;
        int minutes = ticks / (60 * 20);
        ticks -= minutes * 60 * 20;
        int seconds = ticks / 20;

        return (hours != 0 ? "%d %s".formatted(hours, getWordInDeclension(hours, List.of("час", "часа", "часов"))) : "") +
                (hours != 0 && (minutes != 0 || seconds != 0) ? " " : "") +
                (minutes != 0 ? "%d %s".formatted(minutes, getWordInDeclension(minutes, List.of("минуту", "минуты", "минут"))) : "") +
                (minutes != 0 && seconds != 0 ? " " : "") +
                (seconds != 0 ? "%d %s".formatted(seconds, getWordInDeclension(seconds, List.of("секунду", "секунды", "секунд"))) : "");
    }
}