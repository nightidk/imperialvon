package ru.nightidk.imperialvon;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;
import ru.nightidk.imperialvon.init.setup.BothSetup;
import ru.nightidk.imperialvon.init.setup.ClientSetup;
import ru.nightidk.imperialvon.init.setup.ServerSetup;

import java.io.File;

@Mod(ImperialVon.MODID)
public class ImperialVon
{
    public static final String MODID = "imperialvon";
    public static final Logger LOG = LogManager.getLogger("ImperialVon");
    public static File configFile;
    public static File authFile;
    @Getter
    public static Dist envType;
    public static MinecraftServer server;

    public ImperialVon()
    {
        LanguageAdapter.registerMod(MODID);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ServerSetup::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(BothSetup::setup);

        envType = Environment.get().getDist();

        LOG.info("Initialized.");
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }
}
