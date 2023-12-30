package com.corosus.coroutil.loader.fabric;

import com.corosus.modconfig.CoroConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ConfigModFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		//TODO: fabric uses its own classes for client commands, drop this for now
		/*ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
			CommandCoroConfigClientFabric.register(dispatcher);
		}));*/

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			CoroConfigRegistry.instance().allModsConfigsLoadedAndRegisteredHook();
		});
	}
}