package ru.nightidk.imperialvon.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.fml.loading.FMLPaths;
import ru.nightidk.imperialvon.ImperialVon;

import java.io.File;
import java.io.IOException;

public class PlayerDataHandler {

    private static File playerDataFolder = playerDataFolder = new File(FMLPaths.CONFIGDIR.get() + "/ImperialVon/", "player_data");

    public static void savePlayerData(ServerPlayer player) {
        if (!playerDataFolder.exists() && !playerDataFolder.mkdirs()) {
            ImperialVon.LOG.error("Не удалось создать папку для данных игроков: {}", playerDataFolder.getAbsolutePath());
            return;
        }

        CompoundTag playerData = new CompoundTag();
        playerData.putDouble("x", player.getX());
        playerData.putDouble("y", player.getY());
        playerData.putDouble("z", player.getZ());
        playerData.putFloat("yaw", player.getYRot());
        playerData.putFloat("pitch", player.getXRot());
        playerData.putString("dimension", player.getLevel().dimension().location().toString());
        playerData.putInt("gameType", player.gameMode.getGameModeForPlayer().getId());

        File playerFile = new File(playerDataFolder, player.getGameProfile().getName() + ".dat");
        try {
            NbtIo.writeCompressed(playerData, playerFile);
            ImperialVon.LOG.info("Данные игрока {} сохранены.", player.getGameProfile().getName());
        } catch (IOException e) {
            ImperialVon.LOG.error("Ошибка при сохранении данных игрока {}: {}", player.getGameProfile().getName(), e.getMessage());
        }
    }

    public static PlayerDataType loadPlayerData(ServerPlayer player) {
        File playerFile = new File(playerDataFolder, player.getGameProfile().getName() + ".dat");
        if (playerFile.exists()) {
            try {
                CompoundTag playerData = NbtIo.readCompressed(playerFile);

                double x = playerData.getDouble("x");
                double y = playerData.getDouble("y");
                double z = playerData.getDouble("z");
                float yaw = playerData.getFloat("yaw");
                float pitch = playerData.getFloat("pitch");
                ResourceLocation dimension = new ResourceLocation(playerData.getString("dimension"));
                int gameType = playerData.getInt("gameType");

                ServerLevel level = DimensionHelper.getLevelByDimension(ImperialVon.server, dimension);
                if (level == null) {
                    ImperialVon.LOG.error("Не удалось найти измерение: {}", dimension);
                    return null;
                }

                ImperialVon.LOG.info("Данные игрока {} загружены.", player.getGameProfile().getName());

                return new PlayerDataType(new Location(level, x, y, z, yaw, pitch), GameType.byId(gameType));
            } catch (IOException e) {
                ImperialVon.LOG.error("Ошибка при загрузке данных игрока {}: {}", player.getGameProfile().getName(), e.getMessage());
            }
        }
        return null;
    }
}