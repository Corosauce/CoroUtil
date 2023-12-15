package com.corosus.coroutil.loader.fabric;

import com.corosus.coroutil.common.core.modconfig.ConfigMod;
import com.corosus.coroutil.common.core.modconfig.IConfigCategory;
import com.corosus.coroutil.common.core.modconfig.ModConfigData;
import com.corosus.coroutil.common.core.command.CommandCoroConfig;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ConfigModFabric extends ConfigMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("modid");

	@Override
	public void onInitialize() {

		CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
			CommandCoroConfig.register(dispatcher);
		}));

		ModConfigEvents.loading(ConfigMod.instance().MODID).register((ModConfig config) -> {
			ConfigMod.instance().onLoadOrReload(config.getFileName());
		});

		ModConfigEvents.reloading(ConfigMod.instance().MODID).register((ModConfig config) -> {
			ConfigMod.instance().onLoadOrReload(config.getFileName());
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