package com.corosus.coroutil.loader.fabric;

import com.corosus.coroutil.command.CommandCoroConfig;
import com.corosus.modconfig.ConfigMod;
import com.corosus.modconfig.CoroConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;

import java.nio.file.Path;

public class ConfigModFabric extends ConfigMod implements ModInitializer {

	public ConfigModFabric() {
		this.init();
	}

	@Override
	public void onInitialize() {

		CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
			CommandCoroConfig.register(dispatcher);
		}));

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			CoroConfigRegistry.instance().allModsConfigsLoadedAndRegisteredHook();
		});

	}

	@Override
	public Path getConfigPath() {
		return FabricLoader.getInstance().getConfigDir();
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