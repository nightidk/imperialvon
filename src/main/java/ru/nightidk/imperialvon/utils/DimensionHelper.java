package ru.nightidk.imperialvon.utils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class DimensionHelper {
    public static ServerLevel getLevelByDimension(MinecraftServer server, ResourceLocation dimension) {
        return server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, dimension));
    }
}