package com.corosus.coroutil.loader.neoforge;

import com.corosus.coroutil.command.CommandCoroConfig;
import com.corosus.coroutil.command.CommandCoroConfigClient;
import com.corosus.modconfig.CoroConfigRegistry;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class ClientEvents {

    public void onGameTick(ClientTickEvent.Post event) {
        CoroConfigRegistry.instance().allModsConfigsLoadedAndRegisteredHook();
    }

    public void onRegisterCommandsClient(RegisterClientCommandsEvent event) {
        CommandCoroConfigClient.register(event.getDispatcher());
    }

}
