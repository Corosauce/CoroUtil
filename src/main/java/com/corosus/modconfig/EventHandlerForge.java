package com.corosus.modconfig;

import com.corosus.coroconfig.CoroConfigTracker;
import com.corosus.coroconfig.CoroModConfig;
import com.corosus.coroutil.command.CommandReloadConfig;
import com.corosus.coroutil.command.CommandReloadConfigClient;
import com.corosus.coroutil.config.ConfigCoroUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EventHandlerForge {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandReloadConfig.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void registerCommandsClient(RegisterClientCommandsEvent event) {
        CommandReloadConfigClient.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onEntityLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().getGameTime() % 40 == 0) {
            System.out.println("use debug: " + ConfigCoroUtil.useLoggingDebug);
        }
    }

    @SubscribeEvent
    public void serverStart(ServerAboutToStartEvent event) {
    }


}