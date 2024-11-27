package ru.nightidk.imperialvon.init.setup;

import com.mojang.datafixers.util.Pair;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import org.zeith.hammerlib.net.Network;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.commands.*;
import ru.nightidk.imperialvon.configuration.ConfigVariables;
import ru.nightidk.imperialvon.init.WorldsIV;
import ru.nightidk.imperialvon.listeners.serverside.AuthEventListener;
import ru.nightidk.imperialvon.listeners.serverside.ModEventListener;
import ru.nightidk.imperialvon.net.RegionRenderPacket;
import ru.nightidk.imperialvon.utils.*;
import ru.nightidk.imperialvon.utils.region.RegionHelper;
import ru.nightidk.imperialvon.utils.region.RegionPositions;
import ru.nightidk.imperialvon.utils.region.RegionTracker;
import ru.nightidk.imperialvon.utils.region.RegionsHandler;
import ru.nightidk.jdautil.JDAAuth;
import ru.nightidk.jdautil.actions.MessageActions;
import ru.nightidk.jdautil.types.AuthorInfoBase;
import ru.nightidk.jdautil.types.BaseEmbed;

import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.Set;

import static ru.nightidk.imperialvon.ImperialVon.*;
import static ru.nightidk.imperialvon.utils.ChatMessageUtil.getStyledComponent;
import static ru.nightidk.imperialvon.utils.ChatMessageUtil.sendChatMessageToPlayer;
import static ru.nightidk.imperialvon.utils.ConfigUtils.loadAuth;
import static ru.nightidk.imperialvon.utils.ConfigUtils.loadConfig;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.DEDICATED_SERVER)
public class ServerSetup {
    public static void setup(FMLDedicatedServerSetupEvent event) {
        LOG.info("Server logic initialized.");
        LOG.info("Loading config file...");
        configFile = new File(Platform.getConfigFolder().toFile(), "imperialvon.properties");
        loadConfig(configFile);
        LOG.info("Finished loading config file.");

        LOG.info("Loading auth file...");
        File directory = new File(Platform.getModsFolder().toFile() + "/ImperialVon");
        if (!directory.exists())
            if (!directory.mkdirs())
                throw new RuntimeException("Error creating directory for auth config.");
        authFile = new File(Platform.getModsFolder().toFile() + "/ImperialVon/", "auth.json");
        loadAuth(authFile);
        RegionsHandler.loadRegions();
        LOG.info("Finished loading auth file.");


        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> {
            ConfigReload.register(dispatcher);
            RestartCommand.register(dispatcher);
            MaintanceCommand.register(dispatcher);
            AuthCommand.register(dispatcher);
            RegionCommand.register(dispatcher);
            HatCommand.register(dispatcher);
        });
        LOG.info("Commands registered.");



        if (!ConfigVariables.DISCORD_AUTH.isEmpty())
            JDAAuth.auth(ConfigVariables.DISCORD_AUTH);

        ModEventListener.setTickClean(ConfigVariables.TICK_FOR_CLEAN);
        ModEventListener.setTickRestart(ConfigVariables.RESTART_TIME);
        LOG.info("Server tick event registered.");
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event)
    {
        LOG.info("Starting server...");
        MessageActions.editMessage(
                "1250591961988206652",
                ConfigVariables.DISCORD_STATUS_MESSAGE,
                null,
                new BaseEmbed(
                        "Информация об сервере",
                        "Статус: " + (ConfigVariables.MAINTANCE ? "Тех. работы" : "Запускается..."),
                        (ConfigVariables.MAINTANCE ? new Color(255, 136, 0).getRGB() : new Color(255, 219, 0).getRGB()),
                        new AuthorInfoBase("Империя \"von\"")
                )
        );
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        server = event.getServer();
        ModEventListener.serverStartedEvent(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        RegionsHandler.loadRegions();

        ModEventListener.serverStoppingEvent(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        try {

            LOG.info("Сохранение данных всех игроков перед выключением сервера...");
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                if (player.getLevel().dimension() != WorldsIV.VOID)
                    PlayerDataHandler.savePlayerData(player);
            }

            ModEventListener.serverStoppedEvent(event.getServer());

        } catch (InterruptedException e) {
            LOG.error(e);
            LOG.error("JDA error while shutdown, forcing...");
            JDAAuth.forceShutdown();
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ModEventListener.restartServerTickEvent(event.getServer());
        ModEventListener.cleanItemsTickEvent(event.getServer());
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        if (!AuthUtil.isAuthorized(event.getPlayer())) event.setCanceled(true);
        else ModEventListener.onMessage(event.getRawText(), event.getPlayer());
    }

    @SubscribeEvent
    public static void onServerPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ModEventListener.joinedServerEvent(event.getEntity());
    }

    @SubscribeEvent
    public static void onServerPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        ModEventListener.disconnectServerEvent(event.getEntity());
    }

    @SubscribeEvent
    public static void onHitBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getSide() == LogicalSide.CLIENT) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack heldItem = player.getMainHandItem();

            if (heldItem.getItem() == Items.WOODEN_AXE) {
                BlockPos blockPos = event.getPos();
                System.out.println(blockPos);
                Vec3 pos = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                System.out.println(pos);
                RegionPositions region = RegionTracker.getRegion(player);
                if (region == null) {
                    region = RegionTracker.setRegion(player, pos, Vec3.ZERO);
                    sendChatMessageToPlayer(player,
                            getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Первая позиция установлена: " + blockPos.toShortString(), TextStyleUtil.PURPLE.getStyle()))
                    );
                } else {
                    if (!Objects.equals(region.getPos1(), pos))
                        sendChatMessageToPlayer(player,
                                getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Первая позиция установлена: " + blockPos.toShortString(), TextStyleUtil.PURPLE.getStyle()))
                        );
                    region.setPos1(pos);
                }
                Network.sendTo(player, new RegionRenderPacket(region));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() == LogicalSide.CLIENT || event.getHand() != InteractionHand.MAIN_HAND) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack heldItem = player.getMainHandItem();

            if (heldItem.getItem() == Items.WOODEN_AXE) {
                BlockPos blockPos = event.getPos();
                Vec3 pos = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                RegionPositions region = RegionTracker.getRegion(player);
                if (region == null) {
                    region = RegionTracker.setRegion(player, Vec3.ZERO, pos);
                    sendChatMessageToPlayer(player,
                            getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Вторая позиция установлена: " + blockPos.toShortString(), TextStyleUtil.PURPLE.getStyle()))
                    );
                } else {
                    if (!Objects.equals(region.getPos2(), pos))
                        sendChatMessageToPlayer(player,
                                getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Вторая позиция установлена: " + blockPos.toShortString(), TextStyleUtil.PURPLE.getStyle()))
                        );
                    region.setPos2(pos);
                }
                Network.sendTo(player, new RegionRenderPacket(region));
                event.setCanceled(true);
            }
            if (heldItem.getItem() == Items.STICK) {
                BlockPos blockPos = event.getPos();
                Set<ChunkPos> chunkPosSet = RegionHelper.getChunksAroundBlock(blockPos, 1);
                Pair<BlockPos, BlockPos> chunkPosPair = RegionHelper.getFarthestBlocks(chunkPosSet);

                if (chunkPosPair == null) return;

                Vec3 pos1 = new Vec3(chunkPosPair.getFirst().getX(), chunkPosPair.getFirst().getY(), chunkPosPair.getFirst().getZ());
                Vec3 pos2 = new Vec3(chunkPosPair.getSecond().getX(), chunkPosPair.getSecond().getY(), chunkPosPair.getSecond().getZ());

                Vec3 expandedPos1 = RegionHelper.expandToBounds(pos1, pos2, true);
                Vec3 expandedPos2 = RegionHelper.expandToBounds(pos1, pos2, false);

                RegionPositions region = RegionTracker.setRegion(player, expandedPos1, expandedPos2);

                sendChatMessageToPlayer(player,
                        getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(
                                getStyledComponent("Автоматическое выделение региона с позициями: " + RegionHelper.toShortString(expandedPos1) + " и " + RegionHelper.toShortString(expandedPos2) + ".", TextStyleUtil.PURPLE.getStyle())
                        )
                );

                Network.sendTo(player, new RegionRenderPacket(region));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!AuthUtil.isAuthorized(player) && event.getFrom() == WorldsIV.VOID && event.getTo() != WorldsIV.VOID)
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tickPlayer(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player) {
            AuthEventListener.onPlayerTick(event);
            if (!AuthUtil.isAuthorized(player) && player.distanceToSqr(0, 100, 0) >= 0.5)
                Location.ZERO.teleport(player);
        }
    }

    @SubscribeEvent
    public static void allowChatMessage(ServerChatEvent.Preview event) {
        event.setCanceled(ModEventListener.allowChatMessage(event.getPlayer()));
    }

    @SubscribeEvent
    public static void changeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getNewGameMode() != GameType.SPECTATOR && !AuthEventListener.canChangeGameMode(player))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void executeCommand(CommandEvent event) {
        event.setCanceled(AuthEventListener.onPlayerCommand(event.getParseResults().getContext().getSource().getPlayer(), event.getParseResults().getReader().getString()));
    }
}
