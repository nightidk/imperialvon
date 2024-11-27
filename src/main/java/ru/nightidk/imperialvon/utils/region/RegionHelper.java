package ru.nightidk.imperialvon.utils.region;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class RegionHelper {

    public static Vec3 expandToBounds(Vec3 pos1, Vec3 pos2, boolean isLower) {
        double minX = Math.min(pos1.x(), pos2.x());
        double minZ = Math.min(pos1.z(), pos2.z());
        double maxX = Math.max(pos1.x(), pos2.x());
        double maxZ = Math.max(pos1.z(), pos2.z());

        return isLower
                ? new Vec3(minX, 0, minZ)
                : new Vec3(maxX, 255, maxZ);
    }

    public static Set<ChunkPos> getChunksAroundBlock(BlockPos blockPos, int radius) {
        Set<ChunkPos> chunks = new HashSet<>();

        int centerChunkX = blockPos.getX() >> 4;
        int centerChunkZ = blockPos.getZ() >> 4;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                chunks.add(new ChunkPos(centerChunkX + dx, centerChunkZ + dz));
            }
        }

        return chunks;
    }

    public static Pair<BlockPos, BlockPos> getFarthestBlocks(Set<ChunkPos> chunks) {
        if (chunks.isEmpty())
            return null;

        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (ChunkPos chunk : chunks) {
            if (chunk.x < minX) minX = chunk.x;
            if (chunk.z < minZ) minZ = chunk.z;
            if (chunk.x > maxX) maxX = chunk.x;
            if (chunk.z > maxZ) maxZ = chunk.z;
        }

        BlockPos farthestBlock1 = new BlockPos(minX * 16, 0, minZ * 16);
        BlockPos farthestBlock2 = new BlockPos((maxX + 1) * 16 - 1, 0, (maxZ + 1) * 16 - 1);

        return Pair.of(farthestBlock1, farthestBlock2);
    }

    public static String toShortString(Vec3 vec3) {
        return new BlockPos(vec3).toShortString();
    }

    public static boolean doesOverlap(RegionPositions existingRegion, RegionPositions newRegion) {
        Vec3 existingPos1 = existingRegion.getPos1();
        Vec3 existingPos2 = existingRegion.getPos2();

        Vec3 newPos1 = newRegion.getPos1();
        Vec3 newPos2 = newRegion.getPos2();

        boolean xOverlap = newPos1.x <= existingPos2.x && newPos2.x >= existingPos1.x;
        boolean yOverlap = newPos1.y <= existingPos2.y && newPos2.y >= existingPos1.y;
        boolean zOverlap = newPos1.z <= existingPos2.z && newPos2.z >= existingPos1.z;

        return xOverlap && yOverlap && zOverlap;
    }
}
