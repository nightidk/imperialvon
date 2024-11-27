package ru.nightidk.imperialvon.init.setup;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import ru.nightidk.imperialvon.init.ArgumentRegistry;

public class BothSetup {
    public static void setup(FMLCommonSetupEvent event) {
        ArgumentRegistry.registerArguments();
    }
}
