package com.corosus.coroutil.loader.forge;

import com.corosus.coroutil.common.core.modconfig.ConfigMod;
import com.corosus.coroutil.common.core.command.CommandCoroConfig;
import com.corosus.coroutil.common.core.command.CommandCoroConfigClient;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class EventHandlerForge {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandCoroConfig.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void registerCommandsClient(RegisterClientCommandsEvent event) {
        CommandCoroConfigClient.register(event.getDispatcher());
    }

    /*@SubscribeEvent
    public void onEntityLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().getGameTime() % 40 == 0) {
            System.out.println("use debug: " + ConfigCoroUtil.useLoggingDebug);
        }
    }*/

    @SubscribeEvent
    public static void configLoad(ModConfigEvent.Loading event) {
        ConfigMod.instance().onLoadOrReload(event.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void configReload(ModConfigEvent.Reloading event) {
        ConfigMod.instance().onLoadOrReload(event.getConfig().getFileName());
    }
}