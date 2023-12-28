package com.corosus.coroutil.loader.fabric;

import com.corosus.coroutil.common.core.modconfig.ConfigMod;
import com.corosus.coroutil.common.core.modconfig.CoroConfigRegistry;
import com.corosus.coroutil.common.core.modconfig.IConfigCategory;
import com.corosus.coroutil.common.core.modconfig.ModConfigData;
import com.corosus.coroutil.common.core.command.CommandCoroConfig;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;

public class ConfigModFabric extends ConfigMod implements ModInitializer {

	@Override
	public void onInitialize() {

		CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
			CommandCoroConfig.register(dispatcher);
		}));

		ModConfigEvents.loading(ConfigMod.instance().MODID).register((ModConfig config) -> {
			CoroConfigRegistry.instance().onLoadOrReload(config.getFileName());
		});

		ModConfigEvents.reloading(ConfigMod.instance().MODID).register((ModConfig config) -> {
			CoroConfigRegistry.instance().onLoadOrReload(config.getFileName());
		});

	}

	@Override
	public ModConfigData makeLoaderSpecificConfigData(String savePath, String parStr, Class parClass, IConfigCategory parConfig) {
		return new ModConfigDataFabric(savePath, parStr, parClass, parConfig);
	}

	@Override
	public Path getConfigPath() {
		return FabricLoader.getInstance().getConfigDir();
	}
}