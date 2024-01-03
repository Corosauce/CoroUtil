package com.corosus.modconfig;

import com.corosus.coroutil.config.ConfigCoroUtil;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.io.File;
import java.nio.file.Path;

/**
 * Placed in com.corosus.modconfig for backwards compatibility
 */
public abstract class ConfigMod {

    public static final String MODID = "coroutil";

    private static ConfigMod instance;

    public ConfigMod() {
        instance = this;
        new File("./config/CoroUtil").mkdirs();
        CoroConfigRegistry.instance().addConfigFile(MODID, new ConfigCoroUtil());
    }

    public static ConfigMod instance() {
        return instance;
    }

    //TODO: if more needs like this come up, put it in MultiLoaderUtil and setup a class that contains all the methods, including makeLoaderSpecificConfigData
    public abstract Path getConfigPath();

    /**
     * Here for backwards compatibility
     */
    public static void addConfigFile(String modID, IConfigCategory configCat) {
        CoroConfigRegistry.instance().addConfigFile(modID, configCat);
    }

    /**
     * Here for backwards compatibility
     */
    public static void forceSaveAllFilesFromRuntimeSettings() {
        CoroConfigRegistry.instance().forceSaveAllFilesFromRuntimeSettings();
    }

    /**
     * Here for backwards compatibility, dummy empty method because we reload manually later
     */
    public static void onReload(final ModConfigEvent.Reloading configEvent) {

    }
}
