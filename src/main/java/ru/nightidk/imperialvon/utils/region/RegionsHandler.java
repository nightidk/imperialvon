package ru.nightidk.imperialvon.utils.region;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.architectury.platform.Platform;
import net.minecraft.core.BlockPos;
import ru.nightidk.imperialvon.ImperialVon;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class RegionsHandler {
    protected static final File REGION_FILE = new File(Platform.getModsFolder().toFile() + "/ImperialVon/", "regions.json");
    protected static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, RegionManager> regions = new HashMap<>();

    public static void loadRegions() {
        Type mapType = new TypeToken<Map<String, RegionManager>>() {}.getType();
        if (REGION_FILE.exists()) {
            try (Reader reader = new FileReader(REGION_FILE)) {
                Map<String, RegionManager> loadedRegions = gson.fromJson(reader, mapType);
                if (loadedRegions != null) {
                    regions.putAll(loadedRegions);
                }
            } catch (IOException e) {
                ImperialVon.LOG.error(e);
            }
        }
    }

    public static void saveRegions() {
        try (Writer writer = new FileWriter(REGION_FILE)) {
            gson.toJson(regions, writer);
        } catch (IOException e) {
            ImperialVon.LOG.error(e);
        }
    }

    public static void addOrUpdateRegion(String name, RegionManager region) {
        regions.put(name, region);
        saveRegions();
    }

    public static void removeRegion(String name) {
        regions.remove(name);
        saveRegions();
    }

    public static RegionManager getRegion(String name) {
        return regions.get(name);
    }

    public static boolean hasRegion(String name) {
        return regions.containsKey(name);
    }

    public static List<RegionManager> regionsByOwner(String ownerName) {
        return regions.values().stream().filter(region -> Objects.equals(region.owner, ownerName)).toList();
    }

    public static List<RegionManager> getAllRegions() {
        return new ArrayList<>(regions.values());
    }

    public static RegionManager getRegionForPosition(BlockPos pos) {
        return regions.values().stream()
                .filter(region -> isPositionWithinRegion(pos, region.getPositions()))
                .findFirst()
                .orElse(null);
    }

    private static boolean isPositionWithinRegion(BlockPos pos, RegionPositions positions) {
        BlockPos pos1 = new BlockPos(positions.getPos1());
        BlockPos pos2 = new BlockPos(positions.getPos2());
        return pos.getX() >= Math.min(pos1.getX(), pos2.getX()) && pos.getX() <= Math.max(pos1.getX(), pos2.getX())
                && pos.getY() >= Math.min(pos1.getY(), pos2.getY()) && pos.getY() <= Math.max(pos1.getY(), pos2.getY())
                && pos.getZ() >= Math.min(pos1.getZ(), pos2.getZ()) && pos.getZ() <= Math.max(pos1.getZ(), pos2.getZ());
    }
}
