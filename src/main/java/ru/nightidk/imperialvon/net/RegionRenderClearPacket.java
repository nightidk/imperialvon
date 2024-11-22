package ru.nightidk.imperialvon.net;

import net.minecraft.client.Minecraft;
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
public class RegionRenderClearPacket implements IPacket {

    public RegionRenderClearPacket() {}

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void read(FriendlyByteBuf buf) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientExecute(PacketContext ctx) {
        Minecraft.getInstance().execute(() -> {
            RegionRenderer.setRegion(null);
        });
    }

    @Override
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void serverExecute(PacketContext ctx) {
        IPacket.super.serverExecute(ctx);
    }
}
