package com.corosus.coroutil.loader.fabric;

import com.corosus.coroutil.common.core.modconfig.ConfigMod;
import com.corosus.coroutil.common.core.modconfig.IConfigCategory;
import com.corosus.coroutil.common.core.modconfig.ModConfigData;
import com.corosus.coroutil.common.core.command.CommandCoroConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigModFabric extends ConfigMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("modid");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		//TODO: use this for watut
		ServerTickEvents.END_SERVER_TICK.register((minecraftServer) -> {
			for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {

			}
		});

		CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
			CommandCoroConfig.register(dispatcher);
		}));

	}

	@Override
	public ModConfigData makeLoaderSpecificConfigData(String savePath, String parStr, Class parClass, IConfigCategory parConfig) {
		return null;
	}
}