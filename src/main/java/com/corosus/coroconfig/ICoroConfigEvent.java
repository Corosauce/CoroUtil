/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.corosus.coroconfig;

import net.minecraftforge.eventbus.api.Event;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

public interface ICoroConfigEvent {
    record ConfigConfig(Function<CoroModConfig, ICoroConfigEvent> loading, Function<CoroModConfig, ICoroConfigEvent> reloading, @Nullable Function<CoroModConfig, ICoroConfigEvent> unloading) {}

    ConfigConfig CONFIGCONFIG = CoroBindings.getConfigConfiguration().get();


    static ICoroConfigEvent reloading(CoroModConfig modConfig) {
        return CONFIGCONFIG.reloading().apply(modConfig);
    }
    static ICoroConfigEvent loading(CoroModConfig modConfig) {
        return CONFIGCONFIG.loading().apply(modConfig);
    }
    @Nullable static ICoroConfigEvent unloading(CoroModConfig modConfig) {
        return CONFIGCONFIG.unloading() == null ? null : CONFIGCONFIG.unloading().apply(modConfig);
    }
    CoroModConfig getConfig();

    @SuppressWarnings("unchecked")
    default <T extends Event & ICoroConfigEvent> T self() {
        return (T) this;
    }
}
