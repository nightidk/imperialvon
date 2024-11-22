package ru.nightidk.imperialvon.net;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;
import ru.nightidk.imperialvon.utils.region.RegionPositions;
import ru.nightidk.imperialvon.utils.region.RegionRenderer;

@MainThreaded
public class RegionRenderPacket implements IPacket {
    private RegionPositions regionPositions;

    public RegionRenderPacket(RegionPositions positions) {
        this.regionPositions = positions;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(regionPositions.serializeNBT());
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.regionPositions = new RegionPositions(Vec3.ZERO, Vec3.ZERO);
        this.regionPositions.deserializeNBT(buf.readNbt());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientExecute(PacketContext ctx) {
        Minecraft.getInstance().execute(() -> {
            RegionRenderer.setRegion(regionPositions);
        });
    }

    @Override
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void serverExecute(PacketContext ctx) {
        IPacket.super.serverExecute(ctx);
    }
}
