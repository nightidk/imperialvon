package ru.nightidk.imperialvon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.utils.*;

import java.util.Objects;

import static ru.nightidk.imperialvon.utils.AuthUtil.*;
import static ru.nightidk.imperialvon.utils.ChatMessageUtil.getStyledComponent;
import static ru.nightidk.imperialvon.utils.ChatMessageUtil.sendChatMessageToPlayer;

public class AuthCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        final LiteralCommandNode<CommandSourceStack> login = dispatcher.register(
                Commands.literal("login")
                        .then(Commands.argument("password", StringArgumentType.greedyString())
                                .requires(s -> s.getPlayer() != null && !isAuthorized(s.getPlayer()) && isRegistered(s.getTextName()) && s.isPlayer())
                                .executes(context -> {
                                    String password = context.getArgument("password", String.class);
                                    if (context.getSource().getPlayer() == null) return -1;
                                    try {
                                        if (passwordEquals(context.getSource().getTextName(), password)) {
                                            sendChatMessageToPlayer(context.getSource().getPlayer(), getStyledComponent("Успешная авторизация.", TextStyleUtil.GREEN.getStyle()));
                                            setAuthorized(context.getSource().getPlayer());
                                            PlayerDataType playerDataHandler = PlayerDataHandler.loadPlayerData(context.getSource().getPlayer());
                                            if (playerDataHandler == null) {
                                                Level level = ImperialVon.server.getLevel(Level.OVERWORLD);
                                                if (level == null)
                                                    return -1;
                                                new Location(Level.OVERWORLD, level.getSharedSpawnPos()).teleport(context.getSource().getPlayer());
                                                context.getSource().getPlayer().setGameMode(GameType.SURVIVAL);
                                            } else {
                                                playerDataHandler.getLocation().teleport(context.getSource().getPlayer());
                                                context.getSource().getPlayer().setGameMode(playerDataHandler.getGameType());
                                            }
                                        } else {
                                            context.getSource().getPlayer().connection.disconnect(getStyledComponent("Неверный пароль", TextStyleUtil.RED.getStyle()));
                                        }
                                    } catch (Exception e) {
                                        ImperialVon.LOG.error(e);
                                        context.getSource().getPlayer().connection.disconnect(getStyledComponent("Что-то пошло не так. Обратитесь к администрации.", TextStyleUtil.RED.getStyle()));
                                    }
                                    return 0;
                                })
                        )
        );

        final LiteralCommandNode<CommandSourceStack> register = dispatcher.register(
                Commands.literal("register")
                        .then(Commands.argument("password", StringArgumentType.string())
                                .then(Commands.argument("repeatPassword", StringArgumentType.string())
                                    .requires(s -> s.getPlayer() != null && !isAuthorized(s.getPlayer()) && !isRegistered(s.getTextName()) && s.isPlayer())
                                    .executes(context -> {
                                        String password = context.getArgument("password", String.class);
                                        String repeatPassword = context.getArgument("repeatPassword", String.class);
                                        if (context.getSource().getPlayer() == null) return -1;
                                        if (Objects.equals(password, repeatPassword)) {
                                            try {
                                                AuthUtil.register(context.getSource().getTextName(), password, context.getSource().getPlayer().getStringUUID());
                                                sendChatMessageToPlayer(context.getSource().getPlayer(), getStyledComponent("Успешная регистрация.", TextStyleUtil.GREEN.getStyle()));
                                                setAuthorized(context.getSource().getPlayer());
                                                PlayerDataType playerDataHandler = PlayerDataHandler.loadPlayerData(context.getSource().getPlayer());
                                                if (playerDataHandler == null) {
                                                    Level level = ImperialVon.server.getLevel(Level.OVERWORLD);
                                                    if (level == null)
                                                        return -1;
                                                    new Location(Level.OVERWORLD, level.getSharedSpawnPos()).teleport(context.getSource().getPlayer());
                                                    context.getSource().getPlayer().setGameMode(GameType.SURVIVAL);
                                                } else {
                                                    playerDataHandler.getLocation().teleport(context.getSource().getPlayer());
                                                    context.getSource().getPlayer().setGameMode(playerDataHandler.getGameType());
                                                }
                                            } catch (Exception e) {
                                                ImperialVon.LOG.error(e);
                                                context.getSource().getPlayer().connection.disconnect(getStyledComponent("Что-то пошло не так. Обратитесь к администрации.", TextStyleUtil.RED.getStyle()));
                                            }
                                        } else
                                            sendChatMessageToPlayer(context.getSource().getPlayer(), getStyledComponent("Пароли не совпадают.", TextStyleUtil.RED.getStyle()));
                                        return 0;
                                    })
                                )
                        )
        );

        dispatcher.register(Commands.literal("l").redirect(login));
        dispatcher.register(Commands.literal("reg").redirect(register));
    }
}