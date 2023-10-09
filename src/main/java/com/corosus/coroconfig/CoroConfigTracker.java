/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.corosus.coroconfig;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CoroConfigTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Marker CONFIG = MarkerFactory.getMarker("CONFIG");
    public static final CoroConfigTracker INSTANCE = new CoroConfigTracker();
    private final ConcurrentHashMap<String, CoroModConfig> fileMap;
    private final EnumMap<CoroModConfig.Type, Set<CoroModConfig>> configSets;
    private final ConcurrentHashMap<String, Map<CoroModConfig.Type, CoroModConfig>> configsByMod;

    private CoroConfigTracker() {
        this.fileMap = new ConcurrentHashMap<>();
        this.configSets = new EnumMap<>(CoroModConfig.Type.class);
        this.configsByMod = new ConcurrentHashMap<>();
        this.configSets.put(CoroModConfig.Type.CLIENT, Collections.synchronizedSet(new LinkedHashSet<>()));
        this.configSets.put(CoroModConfig.Type.COMMON, Collections.synchronizedSet(new LinkedHashSet<>()));
//        this.configSets.put(CoroModConfig.Type.PLAYER, new ConcurrentSkipListSet<>());
        this.configSets.put(CoroModConfig.Type.SERVER, Collections.synchronizedSet(new LinkedHashSet<>()));
    }

    void trackConfig(final CoroModConfig config) {
        if (this.fileMap.containsKey(config.getFileName())) {
            LOGGER.error(CONFIG,"Detected config file conflict {} between {} and {}", config.getFileName(), this.fileMap.get(config.getFileName()).getModId(), config.getModId());
            throw new RuntimeException("Config conflict detected!");
        }
        this.fileMap.put(config.getFileName(), config);
        this.configSets.get(config.getType()).add(config);
        this.configsByMod.computeIfAbsent(config.getModId(), (k)->new EnumMap<>(CoroModConfig.Type.class)).put(config.getType(), config);
        LOGGER.debug(CONFIG, "Config file {} for {} tracking", config.getFileName(), config.getModId());
    }

    public void loadConfigs(CoroModConfig.Type type, Path configBasePath) {
        LOGGER.debug(CONFIG, "Loading configs type {}", type);
        this.configSets.get(type).forEach(config -> openConfig(config, configBasePath));
    }

    public void unloadConfigs(CoroModConfig.Type type, Path configBasePath) {
        LOGGER.debug(CONFIG, "Unloading configs type {}", type);
        this.configSets.get(type).forEach(config -> closeConfig(config, configBasePath));
    }

    public void openConfig(final CoroModConfig config, final Path configBasePath) {
        LOGGER.trace(CONFIG, "Loading config file type {} at {} for {}", config.getType(), config.getFileName(), config.getModId());
        try {
            final CommentedFileConfig configData = config.getHandler().reader(configBasePath).apply(config);
            config.setConfigData(configData);
            configData.load();
            //config.fireEvent(ICoroConfigEvent.loading(config));
            //config.load();
            config.save();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void closeConfig(final CoroModConfig config, final Path configBasePath) {
        if (config.getConfigData() != null) {
            LOGGER.trace(CONFIG, "Closing config file type {} at {} for {}", config.getType(), config.getFileName(), config.getModId());
            // stop the filewatcher before we save the file and close it, so reload doesn't fire
            config.getHandler().unload(configBasePath, config);
            config.eventUnload();
            var unloading = ICoroConfigEvent.unloading(config);
            if (unloading != null)
                config.fireEvent(unloading);
            config.save();
            config.setConfigData(null);
        }
    }

    public void loadDefaultServerConfigs() {
        configSets.get(CoroModConfig.Type.SERVER).forEach(modConfig -> {
            final CommentedConfig commentedConfig = CommentedConfig.inMemory();
            modConfig.getSpec().correct(commentedConfig);
            modConfig.setConfigData(commentedConfig);
            modConfig.fireEvent(ICoroConfigEvent.loading(modConfig));
            modConfig.eventLoad();
        });
    }

    public String getConfigFileName(String modId, CoroModConfig.Type type) {
        return Optional.ofNullable(configsByMod.getOrDefault(modId, Collections.emptyMap()).getOrDefault(type, null)).
                map(CoroModConfig::getFullPath).map(Object::toString).orElse(null);
    }

    public Map<CoroModConfig.Type, Set<CoroModConfig>> configSets() {
        return configSets;
    }

    public ConcurrentHashMap<String, CoroModConfig> fileMap() {
        return fileMap;
    }
}
