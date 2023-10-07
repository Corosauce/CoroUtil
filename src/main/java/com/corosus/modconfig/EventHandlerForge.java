package com.corosus.modconfig;

import com.corosus.coroutil.command.CommandReloadConfig;
import com.corosus.coroutil.command.CommandReloadConfigClient;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandlerForge {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandReloadConfig.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void registerCommandsClient(RegisterClientCommandsEvent event) {
        CommandReloadConfigClient.register(event.getDispatcher());
    }


}