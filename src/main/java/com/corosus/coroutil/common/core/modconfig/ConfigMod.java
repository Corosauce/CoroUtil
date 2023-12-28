package com.corosus.coroutil.common.core.modconfig;

import com.corosus.coroutil.common.core.config.ConfigCoroUtil;
import com.corosus.coroutil.common.core.util.CULog;

import java.io.File;
import java.nio.file.Path;

public abstract class ConfigMod {

    public static final String MODID = "coroutil";

    private static ConfigMod instance;

    public ConfigMod() {
        instance = this;
        new File("./config/CoroUtil").mkdirs();
        CoroConfigRegistry.instance().addConfigFile(MODID, new ConfigCoroUtil());
    }

    public void registerForgeConfigs() {
        CULog.dbg("registerForgeConfigs");

        CoroConfigRegistry.instance().processQueue();
    }

    public static ConfigMod instance() {
        return instance;
    }

    public abstract ModConfigData makeLoaderSpecificConfigData(String savePath, String parStr, Class parClass, IConfigCategory parConfig);

    public abstract Path getConfigPath();
}
