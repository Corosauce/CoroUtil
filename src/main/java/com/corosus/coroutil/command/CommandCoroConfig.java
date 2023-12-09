package com.corosus.coroutil.command;

import com.corosus.coroutil.util.CULog;
import com.corosus.modconfig.ConfigMod;
import com.corosus.modconfig.ModConfigData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.minecraft.commands.Commands.literal;

public class CommandCoroConfig {
	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			Commands.literal(getCommandName())
			.then(literal("config")
				.then(literal("common")
					.then(argumentReload(ModConfig.Type.COMMON))
					.then(argumentSave())
					.then(argumentGet())
					.then(argumentSet())
				)
			)
		);
	}

	public static ArgumentBuilder<CommandSourceStack, ?> argumentReload(ModConfig.Type type) {
		return literal("reload").executes(c -> {
			CULog.log("reloading all mods common configurations from disk");
			ConfigTracker.INSTANCE.loadConfigs(type, FMLPaths.CONFIGDIR.get());
			c.getSource().sendSuccess(() -> Component.literal("Reloading all common configs from disk"), true);
			return Command.SINGLE_SUCCESS;
		});
	}

	public static ArgumentBuilder<CommandSourceStack, ?> argumentSave() {
		return literal("save").executes(c -> {
			CULog.log("saving all coro mods runtime configs to disk");
			ConfigMod.forceSaveAllFilesFromRuntimeSettings();
			c.getSource().sendSuccess(() -> Component.literal("Saving all common coro configs to disk"), true);
			return Command.SINGLE_SUCCESS;
		});
	}

	public static ArgumentBuilder<CommandSourceStack, ?> argumentGet() {
		return literal("get").then(Commands.argument("file_name", StringArgumentType.word()).suggests((p_136339_, p_136340_) -> SharedSuggestionProvider.suggest(getConfigs(), p_136340_))
				.then(Commands.argument("setting_name", StringArgumentType.word()).suggests((p_136339_, p_136340_) -> SharedSuggestionProvider.suggest(getConfigSettings(StringArgumentType.getString(p_136339_, "file_name")), p_136340_))
						.executes(c -> {
							String fileName = fileToConfig(StringArgumentType.getString(c, "file_name"));
							String configName = ConfigMod.lookupFilePathToConfig.get(fileName).configID;
							String settingName = StringArgumentType.getString(c, "setting_name");
							Object obj = ConfigMod.getField(configName, settingName);
							c.getSource().sendSuccess(() -> Component.literal(settingName + " = " + obj + " in " + fileName), true);
							return Command.SINGLE_SUCCESS;
						})));
	}

	public static ArgumentBuilder<CommandSourceStack, ?> argumentSet() {
		return literal("set").then(Commands.argument("file_name", StringArgumentType.word()).suggests((p_136339_, p_136340_) -> SharedSuggestionProvider.suggest(getConfigs(), p_136340_))
				.then(Commands.argument("setting_name", StringArgumentType.word()).suggests((p_136339_, p_136340_) -> SharedSuggestionProvider.suggest(getConfigSettings(StringArgumentType.getString(p_136339_, "file_name")), p_136340_))
						.then(Commands.argument("value", StringArgumentType.string())
								.executes(c -> {
									String fileName = fileToConfig(StringArgumentType.getString(c, "file_name"));
									String configName = ConfigMod.lookupFilePathToConfig.get(fileName).configID;
									String settingName = StringArgumentType.getString(c, "setting_name");
									String value = StringArgumentType.getString(c, "value");
									boolean result = ConfigMod.updateField(configName, settingName, value);
									if (result) {
										Object obj = ConfigMod.getField(configName, settingName);
										ConfigMod.forceSaveAllFilesFromRuntimeSettings();
										c.getSource().sendSuccess(() -> Component.literal("Set " + settingName + " to " + obj + " in " + fileName), true);
									} else {
										c.getSource().sendSuccess(() -> Component.literal("Invalid setting to use for " + settingName), true);
									}
									return Command.SINGLE_SUCCESS;
								}))));
	}

	public static String getCommandName() {
		return "coro";
	}

	public static Iterable<String> getConfigs() {
		return ConfigMod.lookupFilePathToConfig.keySet().stream().map((e) -> e.replace("\\", "--")).toList();
	}

	public static Iterable<String> getConfigSettings(String config_name) {
		ModConfigData modConfigData = ConfigMod.lookupFilePathToConfig.get(fileToConfig(config_name));
		if (modConfigData != null) {
			List<String> joinedList = new ArrayList<>();
			joinedList.addAll(modConfigData.valsString.keySet());
			joinedList.addAll(modConfigData.valsInteger.keySet());
			joinedList.addAll(modConfigData.valsDouble.keySet());
			joinedList.addAll(modConfigData.valsBoolean.keySet());
			Collections.sort(joinedList);
			return joinedList;
		} else {
			return List.of("<-- invalid config name");
		}
	}

	public static String fileToConfig(String str) {
		return str.replace("--", "\\");
	}
}
