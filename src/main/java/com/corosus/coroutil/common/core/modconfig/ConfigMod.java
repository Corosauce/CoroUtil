package com.corosus.coroutil.common.core.modconfig;

import com.corosus.coroutil.common.core.config.ConfigCoroUtil;
import com.corosus.coroutil.common.core.util.CULog;
import com.corosus.coroutil.common.core.util.OldUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public abstract class ConfigMod {

	private static ConfigMod instance;
	
	public List<ModConfigData> configs = new ArrayList<>();
	public List<ModConfigData> liveEditConfigs = new ArrayList<>();
	public HashMap<String, ModConfigData> lookupRegistryNameToConfig = new HashMap<>();

    //for new forge config, routing reloaded config event to the class to update
	public HashMap<String, ModConfigData> lookupFilePathToConfig = new HashMap<>();

    public final String MODID = "coroutil";

    public static ConfigMod instance() {
        return instance;
    }
	
    public ConfigMod() {
        instance = this;
        new File("./config/CoroUtil").mkdirs();
        addConfigFile(MODID, new ConfigCoroUtil());
    }

    public void onLoadOrReload(String filename) {
        //for new forge config, we set our simple configs field values based on what forge config loaded from file now that the file is fully loaded and ready
        //we cant do this on the fly per field like we used to, forge complains the config builder isnt done yet
        ModConfigData configData = lookupFilePathToConfig.get(filename);
        if (configData != null) {
            dbg("Coro ConfigMod updating runtime values for file: " + filename);
            configData.updateConfigFieldValues();
            configData.configInstance.hookUpdatedValues();
        } else {
            dbg("ERROR, cannot find ModConfigData reference for filename: " + filename);
        }
    }

    public void updateAllConfigsFromForge() {
        for (ModConfigData configData : lookupFilePathToConfig.values()) {
            dbg("Coro ConfigMod updating runtime values for file: " + configData.saveFilePath);
            configData.updateConfigFieldValues();
            configData.configInstance.hookUpdatedValues();
        }
    }

    /*public static void updateConfig(ModConfig config) {
        for (ModConfigData configData : ConfigMod.lookupFilePathToConfig.values()) {
            for (Map.Entry<ModConfig.Type, ModConfig> entrySet : configData.container.configs.entrySet()) {
                if (entrySet.getValue() == config) {
                    dbg("Coro ConfigMod updating runtime values for file: " + configData.saveFilePath);
                    configData.updateConfigFieldValues();
                    configData.configInstance.hookUpdatedValues();
                }
            }
        }
    }*/
    
    public void processHashMap(String modid, Map map) {
    	Iterator it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String name = (String)pairs.getKey();
	        Object val = pairs.getValue();
	        String comment = getComment(modid, name);
	        ConfigEntryInfo info = new ConfigEntryInfo(lookupRegistryNameToConfig.get(modid).configData.size(), name, val, comment);
	        lookupRegistryNameToConfig.get(modid).configData.add(info);
	    }
    }
    
    public static void dbg(Object obj) {
		CULog.dbg("" + obj);
	}
    
    /* Main Usage Methods Start */
    
    /* Main Inits */

    public void addConfigFile(String modID, IConfigCategory configCat) {
    	addConfigFile(modID, configCat.getRegistryName(), configCat, true);
    }
    
    public void addConfigFile(String modID, String categoryName, IConfigCategory configCat, boolean liveEdit) {
    	//if (instance == null) init(event);

        //prevent adding twice
        if (lookupRegistryNameToConfig.containsKey(configCat.getRegistryName())) {
            return;
        }
    	
    	ModConfigData configData = makeLoaderSpecificConfigData(configCat.getConfigFileName(), categoryName, configCat.getClass(), configCat);
    	
    	configs.add(configData);
    	if (liveEdit) liveEditConfigs.add(configData);
    	lookupRegistryNameToConfig.put(categoryName, configData);
        //System.out.println("adding: " + configCat.getConfigFileName() + ".toml");
        lookupFilePathToConfig.put(configCat.getConfigFileName() + ".toml", configData);

    	configData.initData();
    	configData.writeConfigFile(false);
    }

    public abstract ModConfigData makeLoaderSpecificConfigData(String savePath, String parStr, Class parClass, IConfigCategory parConfig);
    
    /* Get Inner Field value */
    public Object getField(String configID, String name) {
    	try { return OldUtil.getPrivateValue(lookupRegistryNameToConfig.get(configID).configClass, instance, name);
    	} catch (Exception ex) { ex.printStackTrace(); }
    	return null;
    }

    /**
     * Return the comment/description associated with a specific field
     * @param configID ID of the config file
     * @param name Name of the value to retrieve from
     * @return The comment associated with the value, null if there is not one or it is not found
     */
    public String getComment(String configID, String name) {
        try {
            Field field = lookupRegistryNameToConfig.get(configID).configClass.getDeclaredField(name);
            ConfigComment anno_comment = field.getAnnotation(ConfigComment.class);
            return anno_comment == null ? "" : anno_comment.value()[0];
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return "";
    }

    /* Update Config Field Entirely */
    public boolean updateField(String configID, String name, Object obj) {
    	if (lookupRegistryNameToConfig.get(configID).setFieldBasedOnType(name, obj)) {
        	//writeHashMapsToFile();
            //invalid here now it just fully re-registers configs which is bad, we just force save outside here now
            //lookupRegistryNameToConfig.get(configID).writeConfigFile(true);
        	return true;
    	}
    	return false;
    }

    public void forceSaveAllFilesFromRuntimeSettings() {
        CULog.dbg("forceSaveAllFilesFromRuntimeSettings invoked");
        for (ModConfigData data : lookupRegistryNameToConfig.values()) {
            //data.writeConfigFile(true);
            data.updateConfigFileWithRuntimeValues();
        }

        //TODO: theres a bug where it takes 2 tries, find out why
        for (ModConfigData data : lookupRegistryNameToConfig.values()) {
            //data.writeConfigFile(true);
            data.updateConfigFileWithRuntimeValues();
        }
    }

    public void forceLoadRuntimeSettingsFromFile() {
        CULog.dbg("forceLoadRuntimeSettingsFromFile invoked");
        for (ModConfigData data : lookupRegistryNameToConfig.values()) {
            //data.reloadRuntimeFromFile();
            data.writeConfigFile(false);
        }
    }

    private final LevelResource SERVERCONFIG = new LevelResource("serverconfig");

    private Path getServerConfigPath(final MinecraftServer server)
    {
        final Path serverConfig = server.getWorldPath(SERVERCONFIG);
        if (!Files.isDirectory(serverConfig)) {
            try {
                Files.createDirectories(serverConfig);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return serverConfig;
    }
    
    /* Main Usage Methods End */
}
