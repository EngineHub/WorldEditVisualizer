package org.enginehub.weviz;

import java.util.concurrent.atomic.AtomicReference;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.enginehub.weviz.net.WECuiPacketHandler;
import org.enginehub.weviz.render.Renderer;
import org.enginehub.weviz.state.VizState;

@Mod(WorldEditVisualizer.MOD_ID)
public class WorldEditVisualizer {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "worldedit-visualizer";

    public WorldEditVisualizer() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::init);
    }

    private void init(FMLCommonSetupEvent event) {
        IModInfo modInfo = ModLoadingContext.get().getActiveContainer().getModInfo();
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            LOGGER.warn("You have installed " + modInfo.getDisplayName() + " on a server." +
                    " This mod does nothing on the server, and will only increase your load time.");
            return;
        }

        AtomicReference<VizState> state = new AtomicReference<>();
        new WECuiPacketHandler(state);

        MinecraftForge.EVENT_BUS.register(new Renderer(state));

        ArtifactVersion version = modInfo.getVersion();
        LOGGER.info(modInfo.getDisplayName() + " for Forge (version " + version + ") is loaded.");
    }

}
