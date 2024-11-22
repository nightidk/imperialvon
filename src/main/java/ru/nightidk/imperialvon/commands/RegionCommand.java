package ru.nightidk.imperialvon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.zeith.hammerlib.net.Network;
import ru.nightidk.imperialvon.configuration.ConfigVariables;
import ru.nightidk.imperialvon.net.RegionRenderClearPacket;
import ru.nightidk.imperialvon.net.RegionRenderPacket;
import ru.nightidk.imperialvon.utils.ChatMessageUtil;
import ru.nightidk.imperialvon.utils.StyledPair;
import ru.nightidk.imperialvon.utils.TextStyleUtil;
import ru.nightidk.imperialvon.utils.region.*;

import java.util.*;

import static ru.nightidk.imperialvon.utils.ChatMessageUtil.*;

public class RegionCommand {
    public static final Map<UUID, RegionPositions> playerRegions = new HashMap<>();
    private static final int CHUNK_SIZE = 16;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        final LiteralCommandNode<CommandSourceStack> regionCommand = dispatcher.register(
                Commands.literal("region")
                        .then(Commands.literal("pos1")
                                .requires(CommandSourceStack::isPlayer)
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player == null) return -1;

                                    BlockPos blockPos = player.blockPosition().below();

                                    Vec3 pos1 = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                                    RegionPositions region = RegionTracker.getRegion(player);
                                    if (region == null) {
                                        region = RegionTracker.setRegion(player, pos1, Vec3.ZERO);
                                    } else {
                                        region.setPos1(pos1);
                                    }
                                    Network.sendTo(player, new RegionRenderPacket(region));

                                    context.getSource().sendSuccess(
                                            Component.literal("Первая позиция установлена: " + blockPos.toShortString()),
                                            false
                                    );

                                    return 1;
                                })
                        )
                        .then(Commands.literal("pos2")
                                .requires(CommandSourceStack::isPlayer)
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player == null) return -1;

                                    BlockPos blockPos = player.blockPosition().below();

                                    Vec3 pos2 = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                                    RegionPositions region = RegionTracker.getRegion(player);
                                    if (region == null) {
                                        region = RegionTracker.setRegion(player, Vec3.ZERO, pos2);
                                    } else {
                                        region.setPos2(pos2);
                                    }

                                    Network.sendTo(player, new RegionRenderPacket(region));

                                    context.getSource().sendSuccess(
                                            Component.literal("Вторая позиция установлена: " + blockPos.toShortString()),
                                            false
                                    );

                                    return 1;
                                })
                        )
                        .then(Commands.literal("claim")
                                .then(Commands.argument("name", StringArgumentType.string())
                                    .requires(CommandSourceStack::isPlayer)
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayer();
                                        if (player == null) return -1;

                                        String regionName = StringArgumentType.getString(context, "name");
                                        RegionPositions region = RegionTracker.getRegion(player);

                                        if (region == null || region.getPos1() == null || region.getPos2() == null) {
                                            context.getSource().sendFailure(
                                                    Component.literal("Вы не установили обе точки (pos1 и pos2).")
                                            );
                                            return -1;
                                        }

                                        if (RegionsHandler.regionsByOwner(player.getName().getString()).size() >= 2) {
                                            context.getSource().sendFailure(
                                                    Component.literal("Вы уже имеете 2 региона.")
                                            );
                                            return -1;
                                        }

                                        if (RegionsHandler.hasRegion(regionName)) {
                                            context.getSource().sendFailure(
                                                    Component.literal("Регион с таким названием уже существует.")
                                            );
                                            return -1;
                                        }

                                        Vec3 pos1 = region.getPos1();
                                        Vec3 pos2 = region.getPos2();

                                        Vec3 expandedPos1 = expandToBounds(pos1, pos2, true);
                                        Vec3 expandedPos2 = expandToBounds(pos1, pos2, false);

                                        region.setPos1(expandedPos1);
                                        region.setPos2(expandedPos2);

                                        Network.sendTo(player, new RegionRenderPacket(region));

                                        if (!isRegionWithinChunkLimits(expandedPos1, expandedPos2, ConfigVariables.MAX_CHUNK_REGION)) {
                                            context.getSource().sendFailure(
                                                    Component.literal("Регион превышает допустимый размер 3x3 чанка.")
                                            );
                                            return -1;
                                        }

                                        RegionManager regionManager = RegionManager.create(region, player.getName().getString(), regionName);

                                        RegionsHandler.addOrUpdateRegion(regionName, regionManager);

                                        context.getSource().sendSuccess(
                                                Component.literal(String.format(
                                                        "Регион \"%s\" создан с границами pos1: %s и pos2: %s",
                                                        regionName, toShortString(expandedPos1), toShortString(expandedPos2)
                                                )),
                                                false
                                        );

                                        return 1;
                                    })
                                )
                        )
                        .then(Commands.literal("info")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .requires(CommandSourceStack::isPlayer)
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            if (player == null) return -1;

                                            String regionName = StringArgumentType.getString(context, "name");

                                            RegionManager regionManager = RegionsHandler.getRegion(regionName);

                                            if (regionManager == null) {
                                                context.getSource().sendFailure(
                                                        Component.literal("Региона с таким названием не существует.")
                                                );
                                                return -1;
                                            }

                                            if (!Objects.equals(regionManager.getOwner(), player.getName().getString()) && !regionManager.getMembers().contains(player.getName().getString())) {
                                                context.getSource().sendFailure(
                                                        Component.literal("У вас нет доступа к этому региону.")
                                                );
                                                return -1;
                                            }

                                            ChatMessageUtil.sendChatMessageToPlayer(player,
                                                    getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponentsWithNewLines(
                                                            new StyledPair("===== Информация об регионе: " + regionName + " =====", TextStyleUtil.YELLOW.getStyle()),
                                                            new StyledPair("Владелец: " + regionManager.getOwner(), TextStyleUtil.YELLOW.getStyle()),
                                                            new StyledPair("Участники: " + String.join(", ", regionManager.getMembers()), TextStyleUtil.YELLOW.getStyle()),
                                                            new StyledPair("Позиции региона: " + regionManager.getPositions().getPos1() + ", " + regionManager.getPositions().getPos2(), TextStyleUtil.YELLOW.getStyle())
                                                    ))
                                            );

                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("flags")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .requires(CommandSourceStack::isPlayer)
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            if (player == null) return -1;

                                            String regionName = StringArgumentType.getString(context, "name");

                                            RegionManager regionManager = RegionsHandler.getRegion(regionName);

                                            if (regionManager == null) {
                                                context.getSource().sendFailure(
                                                        Component.literal("Региона с таким названием не существует.")
                                                );
                                                return -1;
                                            }

                                            if (!Objects.equals(regionManager.getOwner(), player.getName().getString()) && !regionManager.getMembers().contains(player.getName().getString())) {
                                                context.getSource().sendFailure(
                                                        Component.literal("У вас нет доступа к этому региону.")
                                                );
                                                return -1;
                                            }

                                            RegionFlags flags = regionManager.getFlags();

                                            ChatMessageUtil.sendChatMessageToPlayer(player,
                                                    getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponentsWithNewLines(
                                                        new StyledPair("===== Информация об флагах региона: " + regionName + " =====", TextStyleUtil.YELLOW.getStyle()),
                                                        new StyledPair("PVP: " + flags.isPvpEnabled(), TextStyleUtil.YELLOW.getStyle()),
                                                        new StyledPair("Ломание блоков: " + flags.isBlockBreakingEnabled(), TextStyleUtil.YELLOW.getStyle()),
                                                        new StyledPair("Открытие сундуков: " + flags.isChestOpeningEnabled(), TextStyleUtil.YELLOW.getStyle())
                                                    ))
                                            );

                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("clear")
                                .requires(CommandSourceStack::isPlayer)
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player == null) return -1;

                                    RegionTracker.removeRegion(player);

                                    Network.sendTo(player, new RegionRenderClearPacket());

                                    context.getSource().sendSuccess(
                                            Component.literal("Выделение региона убрано."),
                                            false
                                    );

                                    return 1;
                                })
                        )
        );

        dispatcher.register(Commands.literal("rg").redirect(regionCommand));
    }

    private static String toShortString(Vec3 vec3) {
        return new BlockPos(vec3).toShortString();
    }


    private static Vec3 expandToBounds(Vec3 pos1, Vec3 pos2, boolean isLower) {
        double minX = Math.min(pos1.x(), pos2.x());
        double minZ = Math.min(pos1.z(), pos2.z());
        double maxX = Math.max(pos1.x(), pos2.x());
        double maxZ = Math.max(pos1.z(), pos2.z());

        return isLower
                ? new Vec3(minX, 0, minZ)
                : new Vec3(maxX, 255, maxZ);
    }

    private static boolean isRegionWithinChunkLimits(Vec3 pos1, Vec3 pos2, int maxChunks) {
        // Вычисляем минимальные и максимальные значения координат по X и Z
        double minX = Math.min(pos1.x, pos2.x);
        double maxX = Math.max(pos1.x, pos2.x);
        double minZ = Math.min(pos1.z, pos2.z);
        double maxZ = Math.max(pos1.z, pos2.z);

        // Преобразуем мировые координаты в координаты чанков
        int chunkMinX = (int) Math.floor(minX) >> 4;
        int chunkMaxX = (int) Math.floor(maxX) >> 4;
        int chunkMinZ = (int) Math.floor(minZ) >> 4;
        int chunkMaxZ = (int) Math.floor(maxZ) >> 4;

        // Проверяем, входит ли область в заданное количество чанков
        return (chunkMaxX - chunkMinX + 1) <= maxChunks && (chunkMaxZ - chunkMinZ + 1) <= maxChunks;
    }


}
