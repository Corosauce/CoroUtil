package com.corosus.coroutil.loader.forge;

import com.corosus.modconfig.ConfigMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.nio.file.Path;

@Mod(ConfigMod.MODID)
public class ConfigModForge extends ConfigMod {
	
    public ConfigModForge() {
        super();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        EventHandlerForge eventHandlerForge = new EventHandlerForge();
        MinecraftForge.EVENT_BUS.register(eventHandlerForge);
        modEventBus.register(EventHandlerForge.class);
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
