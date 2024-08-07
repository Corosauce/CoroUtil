package com.corosus.coroutil.loader.neoforge;

import com.corosus.modconfig.ConfigMod;
import com.corosus.modconfig.CoroConfigRegistry;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.nio.file.Path;

@Mod(ConfigMod.MODID)
public class ConfigModNeoForge extends ConfigMod {

    public ConfigModNeoForge(ModContainer container) {
        super();

        NeoForge.EVENT_BUS.addListener(this::onServerStarting);

        if (FMLEnvironment.dist.isClient()) {
            ClientEvents clientEvents = new ClientEvents();
            NeoForge.EVENT_BUS.addListener(clientEvents::onRegisterCommandsClient);
            NeoForge.EVENT_BUS.addListener(clientEvents::onGameTick);

        }
    }

    private void onServerStarting(ServerStartingEvent event) {
        CoroConfigRegistry.instance().allModsConfigsLoadedAndRegisteredHook();
    }



    @Override
    public Path getConfigPath() {
        return configFolder;
    }

    @Override
    public void reloadConfigs(String side) {
        if (side.equals("client")) {
            ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, ConfigMod.instance().getConfigPath());
        } else if (side.equals("common")) {
            ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.COMMON, ConfigMod.instance().getConfigPath());
        }
    }


}
