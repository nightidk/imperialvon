package ru.nightidk.imperialvon.listeners.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.utils.region.RegionRenderer;

@Mod.EventBusSubscriber(modid = ImperialVon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EnterWorldHandler {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        RegionRenderer.setRegion(null);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onWorldLoad(net.minecraftforge.event.level.LevelEvent.Load event) {
        RegionRenderer.setRegion(null);
    }
}
