/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.corosus.coroconfig;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.loading.progress.ProgressMeter;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The container that wraps around mods in the system.
 * <p>
 * The philosophy is that individual mod implementation technologies should not
 * impact the actual loading and management of mod code. This class provides
 * a mechanism by which we can wrap actual mod code so that the loader and other
 * facilities can treat mods at arms length.
 * </p>
 *
 * @author cpw
 *
 */

public class CoroModContainerConfig
{
    protected final String modId;
    protected final String namespace;
    //protected final IModInfo modInfo;
    protected final EnumMap<CoroModConfig.Type, CoroModConfig> configs = new EnumMap<>(CoroModConfig.Type.class);
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected Optional<Consumer<ICoroConfigEvent>> configHandler = Optional.empty();

    public CoroModContainerConfig(String modID)
    {
        this.modId = modID;
        // TODO: Currently not reading namespace from configuration..
        this.namespace = this.modId;
        //this.modInfo = info;
    }
    /**
     * @return the modid for this mod
     */
    public final String getModId()
    {
        return modId;
    }

    /**
     * @return the resource prefix for the mod
     */
    public final String getNamespace()
    {
        return namespace;
    }

    public void addConfig(final CoroModConfig modConfig) {
       configs.put(modConfig.getType(), modConfig);
    }

    public void dispatchConfigEvent(ICoroConfigEvent event) {
        configHandler.ifPresent(configHandler->configHandler.accept(event));
    }

    /**
     * Accept an arbitrary event for processing by the mod. Probably posted to an event bus in the lower level container.
     * @param e Event to accept
     */
    protected <T extends Event & IModBusEvent> void acceptEvent(T e) {}
}
