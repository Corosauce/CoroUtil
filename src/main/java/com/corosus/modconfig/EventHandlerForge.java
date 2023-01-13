package com.corosus.modconfig;

import com.corosus.coroutil.command.CommandReloadConfig;
import com.corosus.coroutil.command.CommandReloadConfigClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.thread.EffectiveSide;

public class EventHandlerForge {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandReloadConfig.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void registerCommandsClient(RegisterClientCommandsEvent event) {
        CommandReloadConfigClient.register(event.getDispatcher());
    }


}