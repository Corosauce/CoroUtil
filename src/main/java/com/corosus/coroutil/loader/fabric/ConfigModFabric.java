package com.corosus.coroutil.loader.fabric;

import com.corosus.modconfig.ConfigMod;
import com.corosus.modconfig.CoroConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ConfigModFabric extends ConfigMod implements ModInitializer {

	@Override
	public void onInitialize() {

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			CoroConfigRegistry.instance().allModsConfigsLoadedAndRegisteredHook();
		});

	}

	@Override
	public Path getConfigPath() {
		return FabricLoader.getInstance().getConfigDir();
	}
}