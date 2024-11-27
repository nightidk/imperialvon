package ru.nightidk.imperialvon.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.registries.ForgeRegistries;
import ru.nightidk.imperialvon.ImperialVon;

public interface WorldsIV {
    ResourceKey<Biome> VOID_BIOME = biome(ImperialVon.rl("void"));
    ResourceKey<Level> VOID = dim(ImperialVon.rl("void"));
    ResourceKey<DimensionType> VOID_TYPE = dimType(ImperialVon.rl("void"));

    private static ResourceKey<Level> dim(ResourceLocation id) {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, id);
    }

    private static ResourceKey<DimensionType> dimType(ResourceLocation id) {
        return ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, id);
    }

    private static ResourceKey<Biome> biome(ResourceLocation id) {
        return ResourceKey.create(Registry.BIOME_REGISTRY, id);
    }

//    public static void register() {
//        ForgeRegistries.BIOMES.register(ForgeRegistries.BIOMES, VOID_BIOME.registry(), );
//    }
}
