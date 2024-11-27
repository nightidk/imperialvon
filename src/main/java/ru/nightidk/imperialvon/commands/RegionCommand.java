package ru.nightidk.imperialvon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.zeith.hammerlib.net.Network;
import ru.nightidk.imperialvon.configuration.ConfigVariables;
import ru.nightidk.imperialvon.content.args.RegionFlagArgumentType;
import ru.nightidk.imperialvon.net.RegionRenderClearPacket;
import ru.nightidk.imperialvon.net.RegionRenderPacket;
import ru.nightidk.imperialvon.utils.ChatMessageUtil;
import ru.nightidk.imperialvon.utils.StyledPair;
import ru.nightidk.imperialvon.utils.TextStyleUtil;
import ru.nightidk.imperialvon.utils.region.*;

import java.util.*;

import static ru.nightidk.imperialvon.utils.ChatMessageUtil.*;

public class RegionCommand {
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

                                    sendChatMessageToPlayer(player,
                                            getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Первая позиция установлена: " + blockPos.toShortString(), TextStyleUtil.PURPLE.getStyle()))
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

                                    sendChatMessageToPlayer(player,
                                            getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Вторая позиция установлена: " + blockPos.toShortString(), TextStyleUtil.PURPLE.getStyle()))
                                    );

                                    return 1;
                                })
                        )

                        .then(Commands.literal("claim")
                                .then(Commands.argument("name", StringArgumentType.string())
                                    .suggests((context, builder) -> {
                                        ServerPlayer player = context.getSource().getPlayer();
                                        if (player != null) {
                                            String playerName = player.getName().getString();
                                            RegionsHandler.regionsByOwner(playerName).stream()
                                                    .filter((r) -> r.getName().startsWith(builder.getRemainingLowerCase()))
                                                    .forEach((r) -> builder.suggest(r.getName()));
                                        }
                                        return builder.buildFuture();
                                    })
                                    .requires(CommandSourceStack::isPlayer)
                                    .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayer();
                                        if (player == null) return -1;

                                        String regionName = StringArgumentType.getString(context, "name");
                                        RegionPositions region = RegionTracker.getRegion(player);

                                        if (region == null || region.getPos1() == null || region.getPos2() == null) {
                                            sendChatMessageToPlayer(player,
                                                    getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Вы не установили обе точки (pos1 и pos2).", TextStyleUtil.RED.getStyle()))
                                            );
                                            return -1;
                                        }

                                        if (RegionsHandler.regionsByOwner(player.getName().getString()).size() >= 2) {
                                            sendChatMessageToPlayer(player,
                                                    getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Вы уже имеете 2 региона.", TextStyleUtil.RED.getStyle()))
                                            );
                                            return -1;
                                        }

                                        if (RegionsHandler.hasRegion(regionName)) {
                                            sendChatMessageToPlayer(player,
                                                    getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Регион с таким названием уже существует.", TextStyleUtil.RED.getStyle()))
                                            );
                                            return -1;
                                        }

                                        Vec3 pos1 = region.getPos1();
                                        Vec3 pos2 = region.getPos2();

                                        Vec3 expandedPos1 = RegionHelper.expandToBounds(pos1, pos2, true);
                                        Vec3 expandedPos2 = RegionHelper.expandToBounds(pos1, pos2, false);

                                        for (RegionManager existingRegion : RegionsHandler.getAllRegions()) {
                                            if (RegionHelper.doesOverlap(existingRegion.getPositions(), region)) {
                                                sendChatMessageToPlayer(player,
                                                        getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Ваше выделение задевает другой регион.", TextStyleUtil.RED.getStyle()))
                                                );
                                                return -1;
                                            }
                                        }

                                        region.setPos1(expandedPos1);
                                        region.setPos2(expandedPos2);

                                        Network.sendTo(player, new RegionRenderPacket(region));

                                        if (!isRegionWithinChunkLimits(expandedPos1, expandedPos2, ConfigVariables.MAX_CHUNK_REGION)) {
                                            sendChatMessageToPlayer(player,
                                                    getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Регион превышает допустимый размер 3x3 чанка.", TextStyleUtil.RED.getStyle()))
                                            );
                                            return -1;
                                        }

                                        RegionManager regionManager = RegionManager.create(region, player.getName().getString(), regionName);

                                        RegionsHandler.addOrUpdateRegion(regionName, regionManager);

                                        sendChatMessageToPlayer(player,
                                                getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent(String.format(
                                                        "Регион \"%s\" создан с границами pos1: %s и pos2: %s",
                                                        regionName, RegionHelper.toShortString(expandedPos1), RegionHelper.toShortString(expandedPos2)
                                                ), TextStyleUtil.GREEN.getStyle()))
                                        );

                                        return 1;
                                    })
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            if (player != null) {
                                                String playerName = player.getName().getString();
                                                RegionsHandler.regionsByOwner(playerName).stream()
                                                        .filter((r) -> r.getName().startsWith(builder.getRemainingLowerCase()))
                                                        .forEach((r) -> builder.suggest(r.getName()));
                                            }
                                            return builder.buildFuture();
                                        })
                                        .requires(CommandSourceStack::isPlayer)
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            if (player == null) return -1;

                                            String regionName = StringArgumentType.getString(context, "name");

                                            RegionManager regionManager = RegionsHandler.getRegion(regionName);

                                            if (regionManager == null) {
                                                sendChatMessageToPlayer(player,
                                                        getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Региона с таким названием не существует.", TextStyleUtil.RED.getStyle()))
                                                );
                                                return -1;
                                            }

                                            if (!Objects.equals(regionManager.getOwner(), player.getName().getString())) {
                                                sendChatMessageToPlayer(player,
                                                        getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("У вас нет доступа к этому региону.", TextStyleUtil.RED.getStyle()))
                                                );
                                                return -1;
                                            }

                                            RegionsHandler.removeRegion(regionName);

                                            sendChatMessageToPlayer(player,
                                                    getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Регион \"" + regionName + "\" был удалён.", TextStyleUtil.RED.getStyle()))
                                            );

                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("info")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            if (player != null) {
                                                String playerName = player.getName().getString();
                                                RegionsHandler.regionsByOwner(playerName).stream()
                                                        .filter((r) -> r.getName().startsWith(builder.getRemainingLowerCase()))
                                                        .forEach((r) -> builder.suggest(r.getName()));
                                            }
                                            return builder.buildFuture();
                                        })
                                        .requires(CommandSourceStack::isPlayer)
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            if (player == null) return -1;

                                            String regionName = StringArgumentType.getString(context, "name");

                                            RegionManager regionManager = RegionsHandler.getRegion(regionName);

                                            if (regionManager == null) {
                                                sendChatMessageToPlayer(player,
                                                        getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Региона с таким названием не существует.", TextStyleUtil.RED.getStyle()))
                                                );
                                                return -1;
                                            }

                                            if (!Objects.equals(regionManager.getOwner(), player.getName().getString()) && !regionManager.getMembers().contains(player.getName().getString())) {
                                                sendChatMessageToPlayer(player,
                                                        getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("У вас нет доступа к этому региону.", TextStyleUtil.RED.getStyle()))
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
                                        .suggests((context, builder) -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            if (player != null) {
                                                String playerName = player.getName().getString();
                                                RegionsHandler.regionsByOwner(playerName).stream()
                                                        .filter((r) -> r.getName().startsWith(builder.getRemainingLowerCase()))
                                                        .forEach((r) -> builder.suggest(r.getName()));
                                            }
                                            return builder.buildFuture();
                                        })
                                        .requires(CommandSourceStack::isPlayer)
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            if (player == null) return -1;

                                            String regionName = StringArgumentType.getString(context, "name");

                                            RegionManager regionManager = RegionsHandler.getRegion(regionName);

                                            if (regionManager == null) {
                                                sendChatMessageToPlayer(player,
                                                        getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Региона с таким названием не существует.", TextStyleUtil.RED.getStyle()))
                                                );
                                                return -1;
                                            }

                                            if (!Objects.equals(regionManager.getOwner(), player.getName().getString()) && !regionManager.getMembers().contains(player.getName().getString())) {
                                                sendChatMessageToPlayer(player,
                                                        getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("У вас нет доступа к этому региону.", TextStyleUtil.RED.getStyle()))
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
                        .then(Commands.literal("flag")
                                .then(Commands.argument("regionName", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            if (player != null) {
                                                String playerName = player.getName().getString();
                                                RegionsHandler.regionsByOwner(playerName).stream()
                                                        .filter((r) -> r.getName().startsWith(builder.getRemainingLowerCase()))
                                                        .forEach((r) -> builder.suggest(r.getName()));
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("flagName", RegionFlagArgumentType.flag())
                                            .then(Commands.argument("value", BoolArgumentType.bool())
                                                .requires(CommandSourceStack::isPlayer)
                                                .executes(context -> {
                                                    ServerPlayer player = context.getSource().getPlayer();
                                                    if (player == null) return -1;

                                                    String regionName = StringArgumentType.getString(context, "regionName");

                                                    RegionManager regionManager = RegionsHandler.getRegion(regionName);

                                                    if (regionManager == null) {
                                                        sendChatMessageToPlayer(player,
                                                                getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Региона с таким названием не существует.", TextStyleUtil.RED.getStyle()))
                                                        );
                                                        return -1;
                                                    }

                                                    if (!Objects.equals(regionManager.getOwner(), player.getName().getString())) {
                                                        sendChatMessageToPlayer(player,
                                                                getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("У вас нет доступа к этому региону.", TextStyleUtil.RED.getStyle()))
                                                        );
                                                        return -1;
                                                    }

                                                    RegionFlags.FlagsEnum flag = RegionFlagArgumentType.getFlag(context, "flagName");
                                                    boolean value = BoolArgumentType.getBool(context, "value");

                                                    regionManager.getFlags().setFlagFromEnum(flag, value);

                                                    RegionsHandler.saveRegions();

                                                    ChatMessageUtil.sendChatMessageToPlayer(player,
                                                            getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(
                                                                    getStyledComponent(String.format("Флаг \"%s\" в регионе \"%s\" установлен на %s.", flag.getFlagName(), regionName, value), TextStyleUtil.YELLOW.getStyle())
                                                            )
                                                    );

                                                    return 1;
                                                })
                                            )
                                    )
                                )
                        )
                        .then(Commands.literal("sel")
                                .requires(CommandSourceStack::isPlayer)
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player == null) return -1;

                                    RegionTracker.removeRegion(player);

                                    Network.sendTo(player, new RegionRenderClearPacket());

                                    sendChatMessageToPlayer(player,
                                            getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Выделение региона убрано.", TextStyleUtil.PURPLE.getStyle()))
                                    );

                                    return 1;
                                })
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            if (player != null) {
                                                String playerName = player.getName().getString();
                                                RegionsHandler.regionsByOwner(playerName).stream()
                                                        .filter((r) -> r.getName().startsWith(builder.getRemainingLowerCase()))
                                                        .forEach((r) -> builder.suggest(r.getName()));
                                            }
                                            return builder.buildFuture();
                                        })
                                        .requires(CommandSourceStack::isPlayer)
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            if (player == null) return -1;

                                            String regionName = StringArgumentType.getString(context, "name");

                                            RegionManager regionManager = RegionsHandler.getRegion(regionName);

                                            if (regionManager == null) {
                                                sendChatMessageToPlayer(player,
                                                        getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("Региона с таким названием не существует.", TextStyleUtil.RED.getStyle()))
                                                );
                                                return -1;
                                            }

                                            if (!Objects.equals(regionManager.getOwner(), player.getName().getString()) && !regionManager.getMembers().contains(player.getName().getString())) {
                                                sendChatMessageToPlayer(player,
                                                        getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle()).append(getStyledComponent("У вас нет доступа к этому региону.", TextStyleUtil.RED.getStyle()))
                                                );
                                                return -1;
                                            }

                                            RegionPositions region = RegionTracker.setRegion(player, regionManager.getPositions());

                                            Network.sendTo(player, new RegionRenderPacket(region));

                                            sendChatMessageToPlayer(player,
                                                    getStyledComponent("[IVRegions] ", TextStyleUtil.DARK_AQUA.getStyle())
                                                            .append(getStyledComponent(String.format("Регион \"%s\" выделен.", regionName), TextStyleUtil.GREEN.getStyle()))
                                            );

                                            return 1;
                                        })
                                )
                        )
        );

        dispatcher.register(Commands.literal("rg").redirect(regionCommand));
    }

    private static boolean isRegionWithinChunkLimits(Vec3 pos1, Vec3 pos2, int maxChunks) {
        double minX = Math.min(pos1.x, pos2.x);
        double maxX = Math.max(pos1.x, pos2.x);
        double minZ = Math.min(pos1.z, pos2.z);
        double maxZ = Math.max(pos1.z, pos2.z);

        int chunkMinX = (int) Math.floor(minX) >> 4;
        int chunkMaxX = (int) Math.floor(maxX) >> 4;
        int chunkMinZ = (int) Math.floor(minZ) >> 4;
        int chunkMaxZ = (int) Math.floor(maxZ) >> 4;

        return (chunkMaxX - chunkMinX + 1) <= maxChunks && (chunkMaxZ - chunkMinZ + 1) <= maxChunks;
    }


}
