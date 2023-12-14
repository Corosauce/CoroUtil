package com.corosus.coroutil.common.core.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static net.minecraft.commands.Commands.literal;

public class CommandCoroConfigClient {
	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			Commands.literal(getCommandName())
			.then(literal("config")
				.then(literal("client")
						//.then(CommandCoroConfig.argumentReload(ModConfig.Type.CLIENT))
						.then(CommandCoroConfig.argumentSave())
						.then(CommandCoroConfig.argumentGet())
						.then(CommandCoroConfig.argumentSet())
				)
			)
		);
	}

	public static String getCommandName() {
		return "coro";
	}
}
