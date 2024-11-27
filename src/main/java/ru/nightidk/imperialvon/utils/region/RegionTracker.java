package ru.nightidk.imperialvon.utils.region;

import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionTracker {
    @Getter
    private static final Map<UUID, RegionPositions> activeRegions = new HashMap<>();

    public static RegionPositions setRegion(ServerPlayer player, Vec3 pos1, Vec3 pos2) {
        RegionPositions rp = new RegionPositions(pos1, pos2);
        activeRegions.put(player.getUUID(), rp);
        return rp;
    }

    public static RegionPositions setRegion(ServerPlayer player, RegionPositions positions) {
        activeRegions.put(player.getUUID(), positions);
        return positions;
    }

    public static void removeRegion(ServerPlayer player) {
        if (activeRegions.get(player.getUUID()) != null)
            activeRegions.remove(player.getUUID());
    }

    public static RegionPositions getRegion(Player player) {
        return activeRegions.get(player.getUUID());
    }

}