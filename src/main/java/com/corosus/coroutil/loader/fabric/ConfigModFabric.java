package com.corosus.coroutil.loader.fabric;

import com.corosus.modconfig.ConfigMod;
import com.corosus.modconfig.CoroConfigRegistry;
import com.corosus.modconfig.IConfigCategory;
import com.corosus.modconfig.ModConfigData;
import com.corosus.coroutil.command.CommandCoroConfig;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;

public class ConfigModFabric extends ConfigMod implements ModInitializer {

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
}