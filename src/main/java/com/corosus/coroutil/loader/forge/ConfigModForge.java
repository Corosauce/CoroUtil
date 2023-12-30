package com.corosus.coroutil.loader.forge;

import com.corosus.modconfig.ConfigMod;
import com.corosus.modconfig.IConfigCategory;
import com.corosus.modconfig.ModConfigData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

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
        return FMLPaths.CONFIGDIR.get();
    }
}
