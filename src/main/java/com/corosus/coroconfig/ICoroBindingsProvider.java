/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.corosus.coroconfig;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.I18NParser;

import java.util.function.Supplier;

public interface ICoroBindingsProvider {
    Supplier<IEventBus> getForgeBusSupplier();
    Supplier<I18NParser> getMessageParser();
    Supplier<ICoroConfigEvent.ConfigConfig> getConfigConfiguration();
}
