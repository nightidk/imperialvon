package ru.nightidk.imperialvon.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.zeith.hammerlib.api.io.IAutoNBTSerializable;
import org.zeith.hammerlib.api.io.NBTSerializable;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.init.WorldsIV;

import javax.annotation.Nonnull;

@Getter
public class Location extends Vec3 implements IAutoNBTSerializable {
    private final ResourceKey<Level> dimension;
    @NBTSerializable
    protected final float yaw, pitch;
    public static final Location ZERO = new Location(WorldsIV.VOID, 0, 100, 0, 0, 0);

    public Location(double x, double y, double z) {
        this(Level.OVERWORLD, x, y, z, 0, 0);
    }

    public Location(ResourceKey<Level> dimension, BlockPos blockPos) {
        this(dimension, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0, 0);
    }

    public Location(Level world, double x, double y, double z) {
        this(world.dimension(), x, y, z, 0, 0);
    }

    public Location(Level world, double x, double y, double z, float yaw, float pitch) {
        this(world.dimension(), x, y, z, yaw, pitch);
    }

    public Location(BlockPos blockPos) {
        this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public Location(Vec3 vec3) {
        this(vec3.x(), vec3.y(), vec3.z());
    }

    public Location(Location location) {
        this(location.dimension, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location(ResourceKey<Level> dimension, double x, double y, double z, float yaw, float pitch) {
        super(x, y, z);

        this.dimension = dimension;

        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            yaw = 0f;
            pitch = 0f;
        }

        this.yaw = yaw;
        this.pitch = pitch;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public BlockPos toBlockPos() {
        return new BlockPos(x, y, z);
    }

    public boolean hasDimension() {
        return dimension != null;
    }

    public boolean hasRotation() {
        return Float.isFinite(yaw) && Float.isFinite(pitch);
    }

    @Override
    public @Nonnull String toString() {
        return "Location{" +
                (hasDimension() ? "dimension=" + dimension + ", " : "") +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                (hasRotation() ? ", yaw=" + yaw + ", pitch=" + pitch : "") +
                '}';
    }

    public ResourceKey<Level> getDimension(ResourceKey<Level> fallback) {
        return hasDimension() ? dimension : fallback;
    }

    public String toCommandString() {
        return String.format("/execute in %s run tp @s %f %f %f %f %f",
                this.dimension == null ? "minecraft:overworld" : dimension.location(),
                Math.floor(x * 100) / 100f, Math.floor(y * 100) / 100f, Math.floor(z * 100) / 100f,
                Math.floor(yaw * 100) / 100f, Math.floor(pitch * 100) / 100f).replace(',', '.');
    }

    public void teleport(ServerPlayer player) {
        if(player == null) return;
        if (hasDimension() && getDimension(null).compareTo(player.getLevel().dimension()) != 0) {
            ServerLevel world = player.server.getLevel(getDimension(null));

            if (world == null) {
                ImperialVon.LOG.warn("Не получилось телепортивать игрока на координату {} - мир не найден.", this.toCommandString());
                return;
            }

            if (hasRotation()) {
                player.teleportTo(world, x, y, z, yaw, pitch);
            } else {
                player.teleportTo(world, x, y, z, player.getYRot(), player.getXRot());
            }
        } else if (hasRotation()) {
            player.connection.teleport(x, y, z, yaw, pitch);
        } else {
            player.teleportTo(x, y, z);
        }
    }
}
