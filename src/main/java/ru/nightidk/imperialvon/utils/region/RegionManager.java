package ru.nightidk.imperialvon.utils.region;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class RegionManager {
    @Getter
    protected final RegionPositions positions;
    @Getter
    @Setter
    protected String owner;
    @Getter
    protected final Set<String> members = new HashSet<>();
    @Getter
    @Setter
    protected String name;

    @Getter
    protected RegionFlags flags;

    protected RegionManager(RegionPositions positions, String owner, String name) {
        this.positions = positions;
        setOwner(owner);
        setName(name);
        this.flags = new RegionFlags();
    }

    public static RegionManager create(RegionPositions positions, String owner, String name) {
        return new RegionManager(positions, owner, name);
    }

    public void addMember(String playerName) { members.add(playerName); }
    public void removeMember(String playerName) { members.remove(playerName); }

}
