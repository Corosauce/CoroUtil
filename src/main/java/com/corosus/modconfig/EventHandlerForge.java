package com.corosus.modconfig;

import com.corosus.coroutil.command.CommandCoroConfig;
import com.corosus.coroutil.command.CommandCoroConfigClient;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    public void serverStart(ServerAboutToStartEvent event) {
    }


}