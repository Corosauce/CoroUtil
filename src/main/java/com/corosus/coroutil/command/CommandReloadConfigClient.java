package com.corosus.coroutil.command;

import com.corosus.coroconfig.CoroConfigTracker;
import com.corosus.coroconfig.CoroModConfig;
import com.corosus.coroutil.util.CULog;
import com.corosus.modconfig.ConfigMod;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import static net.minecraft.commands.Commands.literal;

public class CommandReloadConfigClient {
	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal(getCommandName())
						.then(literal("config")
								.then(literal("reload")
										.then(Commands.literal("client").executes(c -> {
											CULog.log("reloading all mods client configurations from disk");
											ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());
											CoroConfigTracker.INSTANCE.loadConfigs(CoroModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());
											c.getSource().sendSuccess(() -> Component.literal("Reloading all client configs from disk"), true);
											return Command.SINGLE_SUCCESS;
										}))
								)
								.then(literal("save")
										.then(Commands.literal("client").executes(c -> {
											CULog.log("saving all coro mods runtime configs to disk");
											/** dummy literal for autocomplete sake, see EventHandlerForge.clientChat for what actually "intercepts" this */
											ConfigMod.forceSaveAllFilesFromRuntimeSettings();
											c.getSource().sendSuccess(() -> Component.literal("Saving all client coro configs to disk"), true);
											return Command.SINGLE_SUCCESS;
										}))
								)
						)
		);
	}

	public static String getCommandName() {
		return "coro";
	}
}
