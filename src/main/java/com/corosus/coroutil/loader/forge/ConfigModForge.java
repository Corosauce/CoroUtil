package com.corosus.coroutil.loader.forge;

import com.corosus.coroutil.common.core.modconfig.ConfigMod;
import com.corosus.coroutil.common.core.modconfig.IConfigCategory;
import com.corosus.coroutil.common.core.modconfig.ModConfigData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ConfigModForge.MODID)
public class ConfigModForge extends ConfigMod {

    public static final String MODID = "coroutil";
	
    public ConfigModForge() {
        super();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        EventHandlerForge eventHandlerForge = new EventHandlerForge();
        MinecraftForge.EVENT_BUS.register(eventHandlerForge);
        modEventBus.register(EventHandlerForge.class);
    }

    @Override
    public ModConfigData makeLoaderSpecificConfigData(String savePath, String parStr, Class parClass, IConfigCategory parConfig) {
        return new ModConfigDataForge(savePath, parStr, parClass, parConfig);
    }
}
