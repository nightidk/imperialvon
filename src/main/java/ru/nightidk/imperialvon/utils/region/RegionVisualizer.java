package ru.nightidk.imperialvon.utils.region;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class RegionVisualizer {

    public static void showParticle(ServerPlayer player, Vec3 pos) {
        player.connection.send(new ClientboundLevelParticlesPacket(
                ParticleTypes.HAPPY_VILLAGER,
                true,
                pos.x() + 0.5d, pos.y() + 1.0d, pos.z() + 0.5d,
                0.0f, 0.0f, 0.0f,
                0, 1
        ));
    }

    public static void showRegionFrame(ServerPlayer player, Vec3 pos1, Vec3 pos2, int step) {
        if (pos1 == null || pos2 == null) return;

        double minX = Math.min(pos1.x(), pos2.x());
        double maxX = Math.max(pos1.x(), pos2.x());
        double minY = Math.min(pos1.y(), pos2.y());
        double maxY = Math.min(Math.max(pos1.y(), pos2.y()), 255); // Не выше 255
        double minZ = Math.min(pos1.z(), pos2.z());
        double maxZ = Math.max(pos1.z(), pos2.z());

        // Визуализация верхней и нижней границ
        for (double x = minX; x <= maxX; x += step) {
            for (double z = minZ; z <= maxZ; z += step) {
                showParticle(player, new Vec3(x, minY, z)); // Нижняя граница
                showParticle(player, new Vec3(x, maxY, z)); // Верхняя граница
            }
        }

        // Визуализация боковых граней (по периметру)
        for (double y = minY; y <= maxY; y += step) {
            for (double x = minX; x <= maxX; x += step) {
                showParticle(player, new Vec3(x, y, minZ)); // Граница Z (минимальная)
                showParticle(player, new Vec3(x, y, maxZ)); // Граница Z (максимальная)
            }
            for (double z = minZ; z <= maxZ; z += step) {
                showParticle(player, new Vec3(minX, y, z)); // Граница X (минимальная)
                showParticle(player, new Vec3(maxX, y, z)); // Граница X (максимальная)
            }
        }
    }
}