package ru.nightidk.imperialvon.listeners.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.utils.region.RegionRenderer;

@Mod.EventBusSubscriber(modid = ImperialVon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderEventHandler {
    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            RegionRenderer.render(event);
        }
    }
}