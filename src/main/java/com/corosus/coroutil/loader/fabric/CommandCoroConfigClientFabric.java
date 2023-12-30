package com.corosus.coroutil.loader.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class CommandCoroConfigClientFabric {
	public static void register(final CommandDispatcher<FabricClientCommandSource> dispatcher) {
		/*dispatcher.register(
			ClientCommandManager.literal(getCommandName())
			.then(ClientCommandManager.literal("config")
				.then(ClientCommandManager.literal("client")
						//.then(CommandCoroConfig.argumentReload(ModConfig.Type.CLIENT))
						.then(CommandCoroConfig.argumentSave())
						.then(CommandCoroConfig.argumentGet())
						.then(CommandCoroConfig.argumentSet())
				)
			)
		);*/
	}

	public static String getCommandName() {
		return "coro";
	}
}
