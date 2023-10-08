/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.corosus.coroconfig;

import cpw.mods.modlauncher.util.ServiceLoaderUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.I18NParser;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.ServiceLoader;
import java.util.function.Supplier;

public class CoroBindings {
    private static final CoroBindings INSTANCE = new CoroBindings();

    private final ICoroBindingsProvider provider;

    private CoroBindings() {
        final var providers = ServiceLoaderUtils.streamServiceLoader(()->ServiceLoader.load(FMLLoader.getGameLayer(), ICoroBindingsProvider.class), sce->{}).toList();
        if (providers.size() != 1) {
            throw new IllegalStateException("Could not find bindings provider");
        }
        this.provider = providers.get(0);
    }

    public static Supplier<IEventBus> getForgeBus() {
        return INSTANCE.provider.getForgeBusSupplier();
    }

    public static Supplier<I18NParser> getMessageParser() {
        return INSTANCE.provider.getMessageParser();
    }

    public static Supplier<ICoroConfigEvent.ConfigConfig> getConfigConfiguration() {
        return INSTANCE.provider.getConfigConfiguration();
    }
}
