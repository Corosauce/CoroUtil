package com.corosus.coroutil.loader.forge;

import com.corosus.modconfig.ConfigMod;
import com.corosus.modconfig.CoroConfigRegistry;
import com.corosus.coroutil.command.CommandCoroConfig;
import com.corosus.coroutil.command.CommandCoroConfigClient;
import com.corosus.coroutil.util.CULog;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public class EventHandlerForge {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandCoroConfig.register(event.getDispatcher());
    }

    /*@SubscribeEvent
    public void registerCommandsClient(RegisterClientCommandsEvent event) {
        CommandCoroConfigClient.register(event.getDispatcher());
    }*/

    /*@SubscribeEvent
    public void onEntityLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().getGameTime() % 40 == 0) {
            System.out.println("use debug: " + ConfigCoroUtil.useLoggingDebug);
        }
    }*/

    @SubscribeEvent
    public void serverAboutToStart(ServerAboutToStartEvent event) {
        //i cant get a non async hook between every mod being loaded and the configs being loaded, so for forge we are just going to update the configs on main menu / server start
        CoroConfigRegistry.instance().allModsConfigsLoadedAndRegisteredHook();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onGameTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            CoroConfigRegistry.instance().allModsConfigsLoadedAndRegisteredHook();
        }
    }


}