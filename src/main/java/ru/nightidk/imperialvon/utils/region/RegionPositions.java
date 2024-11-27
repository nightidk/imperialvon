package ru.nightidk.imperialvon.utils.region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;
import org.zeith.hammerlib.api.io.IAutoNBTSerializable;
import org.zeith.hammerlib.api.io.NBTSerializable;

@Setter
@Getter
@AllArgsConstructor
public class RegionPositions implements IAutoNBTSerializable {
    @NBTSerializable
    protected Vec3 pos1;
    @NBTSerializable
    protected Vec3 pos2;
}