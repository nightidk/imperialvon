package ru.nightidk.imperialvon.listeners.serverside;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.zeith.hammerlib.net.Network;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.commands.RegionCommand;
import ru.nightidk.imperialvon.configuration.AuthVariables;
import ru.nightidk.imperialvon.configuration.ConfigVariables;
import ru.nightidk.imperialvon.init.WorldsIV;
import ru.nightidk.imperialvon.net.RegionRenderClearPacket;
import ru.nightidk.imperialvon.utils.*;
import ru.nightidk.imperialvon.utils.region.RegionTracker;
import ru.nightidk.jdautil.JDAAuth;
import ru.nightidk.jdautil.JDAUtil;
import ru.nightidk.jdautil.actions.MessageActions;
import ru.nightidk.jdautil.types.AuthorInfoBase;
import ru.nightidk.jdautil.types.BaseEmbed;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.nightidk.imperialvon.utils.AuthUtil.*;
import static ru.nightidk.imperialvon.utils.ChatMessageUtil.*;

public class ModEventListener {
    @Setter
    protected static int tickClean = 36000;
    @Getter
    @Setter
    protected static int tickRestart = -1;
    public static int tickForPlannedRestart = 0;
//    @Getter
//    protected static List<String> nonDeletableItems = List.of(
//        LibItemNames.MANA_TABLET, LibItemNames.MANA_RING, LibItemNames.MANA_RING_GREATER,
//        LibItemNames.TERRA_PICK, "poisoned_matrix_ingot", "clean_matrix_ingot",
//        "poisoneddropofblood"
//    );

    @SuppressWarnings("unchecked")
    public static void cleanItemsTickEvent(MinecraftServer server) {
        if (!ConfigVariables.AUTORESTART) return;
        tickClean--;
        if (tickClean == 6000) sendChatMessageToAll(server.getPlayerList().getPlayers(), getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent(" 5 минут до очистки предметов.", TextStyleUtil.WHITE.getStyle())), ChatMessageUtil.MessageType.CLEAN);
        if (tickClean == 1200) sendChatMessageToAll(server.getPlayerList().getPlayers(), getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent(" 1 минута до очистки предметов.", TextStyleUtil.WHITE.getStyle())), ChatMessageUtil.MessageType.CLEAN);
        if (tickClean == 600) sendChatMessageToAll(server.getPlayerList().getPlayers(), getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent(" 30 секунд до очистки предметов.", TextStyleUtil.WHITE.getStyle())), ChatMessageUtil.MessageType.CLEAN);
        if (tickClean == 200) sendChatMessageToAll(server.getPlayerList().getPlayers(), getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent(" 10 секунд до очистки предметов.", TextStyleUtil.WHITE.getStyle())), ChatMessageUtil.MessageType.CLEAN);
        if (tickClean <= 100 && tickClean != 0 && tickClean % 20 == 0)
            sendChatMessageToAll(
                    server.getPlayerList().getPlayers(),
                    getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                            .append(getStyledComponent(" " + tickClean / 20 + " " + getWordInDeclension(tickClean / 20, List.of("секунда", "секунды", "секунд")) + " до очистки предметов.", TextStyleUtil.WHITE.getStyle())),
                    ChatMessageUtil.MessageType.CLEAN
            );
        if (tickClean > 0) return;
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return;
        List<ItemEntity> entityList = (List<ItemEntity>) level.getEntities(EntityTypeTest.forClass(ItemEntity.class), (i) -> {
//            Identifier id = Registries.ITEM.getId(i.getStack().getItem());
//            return !getNonDeletableItems().contains(id.getPath());
            return true;
        });
        sendChatMessageToAll(
                server.getPlayerList().getPlayers(),
                getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                        .append(getStyledComponent(" Очищено " + entityList.size() + " " + getWordInDeclension(entityList.size(), List.of("предмет", "предмета", "предметов")) + ".", TextStyleUtil.WHITE.getStyle())),
                ChatMessageUtil.MessageType.CLEAN
        );
        entityList.forEach(Entity::discard);
        tickClean = ConfigVariables.TICK_FOR_CLEAN;
    }

    public static void restartServerTickEvent(MinecraftServer server) {
        if (tickRestart == -1) return;

        tickRestart--;

        int seconds = tickRestart / 20;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        if (hours != 0) return;

        if (minutes > 0 && (minutes % 10 == 0 || minutes == 1 || minutes == 5) && tickRestart % 20 == 0 && seconds >= 60 && seconds % 60 == 0) {
            sendChatMessageToAll(
                    server.getPlayerList().getPlayers(),
                    getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                            .append(getStyledComponent(" Перезагрузка сервера через " + minutes + " " + getWordInDeclension(minutes, List.of("минуту", "минуты", "минут")) + ".", TextStyleUtil.WHITE.getStyle())),
                    ChatMessageUtil.MessageType.RESTART
            );
            return;
        }

        if (seconds == 30 && tickRestart % 20 == 0) {
            sendChatMessageToAll(
                    server.getPlayerList().getPlayers(),
                    getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                            .append(getStyledComponent(" Перезагрузка сервера через 30 секунд.", TextStyleUtil.WHITE.getStyle())),
                    ChatMessageUtil.MessageType.RESTART
            );
            return;
        }
        if (seconds <= 10 && seconds % 5 == 0 && tickRestart % 20 == 0 && seconds > 5) {
            sendChatMessageToAll(
                    server.getPlayerList().getPlayers(),
                    getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                            .append(getStyledComponent(" Перезагрузка сервера через " + seconds + " " + getWordInDeclension(seconds, List.of("секунду", "секунды", "секунд")) + ".", TextStyleUtil.WHITE.getStyle())),
                    ChatMessageUtil.MessageType.RESTART
            );
            return;
        }

        if (seconds <= 5 && seconds != 0 && tickRestart % 20 == 0) {
            sendChatMessageToAll(
                    server.getPlayerList().getPlayers(),
                    getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                            .append(getStyledComponent(" Перезагрузка сервера через " + seconds + " " + getWordInDeclension(seconds, List.of("секунду", "секунды", "секунд")) + ".", TextStyleUtil.WHITE.getStyle())),
                    ChatMessageUtil.MessageType.RESTART
            );
            return;
        }

        if (tickRestart == 0) {
            tickRestart = -1;
            sendChatMessageToAll(
                    server.getPlayerList().getPlayers(),
                    getStyledComponent("[Оповещение]", TextStyleUtil.DARK_AQUA.getStyle())
                            .append(getStyledComponent(" Перезагрузка сервера...", TextStyleUtil.WHITE.getStyle())),
                    ChatMessageUtil.MessageType.NOTIFY
            );
            server.getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.connection.disconnect(getStyledComponent("Перезагрузка сервера...", TextStyleUtil.DARK_AQUA.getStyle())));
            server.halt(false);
        }
    }

    public static void serverStoppingEvent(MinecraftServer server) {
        MessageActions.editMessage(
            "1250591961988206652",
            ConfigVariables.DISCORD_STATUS_MESSAGE,
            null,
            new BaseEmbed(
                    "Информация об сервере",
                    "Статус: Выключается",
                    new Color(255, 0, 0).getRGB(),
                    new AuthorInfoBase("Империя \"von\"")
            )
        );
    }

    public static void serverStoppedEvent(MinecraftServer server) throws InterruptedException {
        ImperialVon.LOG.info( "Restarting server..." );
        if (JDAUtil.JDA != null) {
            MessageActions.editMessage(
                    "1250591961988206652",
                    ConfigVariables.DISCORD_STATUS_MESSAGE,
                    null,
                    new BaseEmbed(
                            "Информация об сервере",
                            "Статус: Отключён",
                            new Color(255, 0, 0).getRGB(),
                            new AuthorInfoBase("Империя \"von\"")
                    )
            );

            JDAAuth.logout();
        }
    }

    public static MutableComponent joinServerEvent(GameProfile gameProfile) {
        if (ImperialVon.server == null) return null;
        UserWhiteList whiteList= ImperialVon.server.getPlayerList().getWhiteList();
        if (ConfigVariables.MAINTANCE && !whiteList.isWhiteListed(gameProfile))
            return getStyledComponent("На сервере ведутся технические работы.", TextStyleUtil.RED.getStyle());
        return null;
    }

    public static void onMessage(String playerChatMessage, ServerPlayer serverPlayer) {
        if (!(serverPlayer instanceof ServerPlayer)) return;
        MessageActions.sendMessage(
                "1250613189658411009",
                "**" + serverPlayer.getDisplayName().getString() + "**: " + playerChatMessage,
                null
        );
    }

    public static void serverStartedEvent(MinecraftServer server1) {
        MessageActions.editMessage(
                "1250591961988206652",
                ConfigVariables.DISCORD_STATUS_MESSAGE,
                null,
                new BaseEmbed(
                        "Информация об сервере",
                        "Статус: " + (ConfigVariables.MAINTANCE ? "Тех. работы" : "Работает"),
                        (ConfigVariables.MAINTANCE ? new Color(255, 136, 0).getRGB() : new Color(0, 255, 0).getRGB()),
                        new AuthorInfoBase("Империя \"von\"")
                )
        );
        if (ConfigVariables.MAINTANCE) return;
        MessageActions.sendMessage(
                "1250613189658411009",
                "[SYSTEM] Сервер запущен.",
                null
        );
    }

    public static void joinedServerEvent(Player player) {
        if (player.getServer() == null) return;
        ServerPlayer serverPlayer = player.getServer().getPlayerList().getPlayer(player.getUUID());
        if (ConfigVariables.MAINTANCE) {
            setAuthorized(serverPlayer, true);
            return;
        }

        setAuthorized(serverPlayer, false);
        sendChatMessageToPlayer(serverPlayer, getAuthMessage(serverPlayer));

        serverPlayer.setInvisible(true);
        serverPlayer.setInvulnerable(true);

        RegionTracker.removeRegion(serverPlayer);
        Network.sendTo(serverPlayer, new RegionRenderClearPacket());

        serverPlayer.setGameMode(GameType.SPECTATOR);

        new Location(WorldsIV.VOID, 0, 100, 0, 0, 0).teleport(serverPlayer);

        AuthVariables.authTicks.add(new Pair<>(player.getName().getString(), 300 * 20));

        if (ConfigVariables.MAINTANCE) return;

        MessageActions.sendMessage(
                "1250613189658411009",
                "[SYSTEM] " + serverPlayer.getName().getString() + " зашёл на сервер.",
                null
        );
    }

    public static void disconnectServerEvent(Player player) {
        if (player.getServer() == null) return;
        ServerPlayer serverPlayer = player.getServer().getPlayerList().getPlayer(player.getUUID());
        if (serverPlayer == null) return;
        setAuthorized(serverPlayer, false);
        if (serverPlayer.getLevel().dimension() != WorldsIV.VOID)
            PlayerDataHandler.savePlayerData(serverPlayer);

        Optional<Pair<String, Integer>> ticks = AuthVariables.authTicks.stream().filter(p -> Objects.equals(p.getKey(), player.getName().getString())).findFirst();
        ticks.ifPresent((s) -> AuthVariables.authTicks.remove(s));
        if (ConfigVariables.MAINTANCE) return;
        MessageActions.sendMessage(
                "1250613189658411009",
                "[SYSTEM] " + serverPlayer.getName().getString() + " вышел с сервера.",
                null
        );
    }

    public static boolean allowChatMessage(ServerPlayer player) {
        if (player == null) return true;
        return !isAuthorized(player);
    }
}