package ru.nightidk.imperialvon.utils.region;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class RegionManager {
    protected final RegionPositions positions;
    @Setter
    protected String owner;
    protected final Set<String> members = new HashSet<>();
    @Setter
    protected String name;

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
