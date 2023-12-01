package com.corosus.modconfig;

import com.corosus.coroconfig.CoroConfigTracker;
import com.corosus.coroconfig.CoroModConfig;
import com.corosus.coroutil.config.ConfigCoroUtil;
import com.corosus.coroutil.util.CULog;
import com.corosus.coroutil.util.OldUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Mod(ConfigMod.MODID)
public class ConfigMod {

	public static ConfigMod instance;
	
	public static List<ModConfigData> configs = new ArrayList<>();
	public static List<ModConfigData> liveEditConfigs = new ArrayList<>();
	public static HashMap<String, ModConfigData> lookupRegistryNameToConfig = new HashMap<>();

    //for new forge config, routing reloaded config event to the class to update
	public static HashMap<String, ModConfigData> lookupFilePathToConfig = new HashMap<>();

    public static final String MODID = "coroutil";
	
    public ConfigMod() {
        MinecraftForge.EVENT_BUS.addListener(this::serverStart);

        EventHandlerForge eventHandlerForge = new EventHandlerForge();
        MinecraftForge.EVENT_BUS.register(eventHandlerForge);

        new File("./config/CoroUtil").mkdirs();
        ConfigMod.addConfigFile(MODID, new ConfigCoroUtil());
    }



    @SubscribeEvent
    public void serverStart(ServerStartingEvent event) {
        //force a full update right before server starts because forge file watching is unreliable
        //itll randomly not invoke ModConfig.Reloading for configs and stick with old values
        dbg("Performing a full config mod force sync");

        //this is where this should go if we ever add server support for our config mod setup
        //CoroConfigTracker.INSTANCE.loadConfigs(CoroModConfig.Type.SERVER, getServerConfigPath(event.getServer()));

        //also done instantly per config now
        //updateAllConfigsFromForge();
    }

    public static void onReload(final ModConfigEvent.Reloading configEvent) {
        //for new forge config, we set our simple configs field values based on what forge config loaded from file now that the file is fully loaded and ready
        //we cant do this on the fly per field like we used to, forge complains the config builder isnt done yet
        ModConfigData configData = ConfigMod.lookupFilePathToConfig.get(configEvent.getConfig().getFileName());
        if (configData != null) {
            dbg("Coro ConfigMod updating runtime values for file: " + configEvent.getConfig().getFileName());
            configData.updateConfigFieldValues();
            configData.configInstance.hookUpdatedValues();
        } else {
            dbg("ERROR, cannot find ModConfigData reference for filename: " + configEvent.getConfig().getFileName());
        }
    }

    public static void updateAllConfigsFromForge() {
        for (ModConfigData configData : ConfigMod.lookupFilePathToConfig.values()) {
            dbg("Coro ConfigMod updating runtime values for file: " + configData.saveFilePath);
            configData.updateConfigFieldValues();
            configData.configInstance.hookUpdatedValues();
        }
    }

    public static void updateConfig(CoroModConfig config) {
        for (ModConfigData configData : ConfigMod.lookupFilePathToConfig.values()) {
            for (Map.Entry<CoroModConfig.Type, CoroModConfig> entrySet : configData.container.configs.entrySet()) {
                if (entrySet.getValue() == config) {
                    dbg("Coro ConfigMod updating runtime values for file: " + configData.saveFilePath);
                    configData.updateConfigFieldValues();
                    configData.configInstance.hookUpdatedValues();
                }
            }
        }
    }
    
    public static void processHashMap(String modid, Map map) {
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

    public static void addConfigFile(String modID, IConfigCategory configCat) {
    	addConfigFile(modID, configCat.getRegistryName(), configCat, true);
    }
    
    public static void addConfigFile(String modID, String categoryName, IConfigCategory configCat, boolean liveEdit) {
    	//if (instance == null) init(event);

        //prevent adding twice
        if (lookupRegistryNameToConfig.containsKey(configCat.getRegistryName())) {
            return;
        }
    	
    	ModConfigData configData = new ModConfigData(configCat.getConfigFileName(), categoryName, configCat.getClass(), configCat);
    	
    	configs.add(configData);
    	if (liveEdit) liveEditConfigs.add(configData);
    	lookupRegistryNameToConfig.put(categoryName, configData);
        //System.out.println("adding: " + configCat.getConfigFileName() + ".toml");
        lookupFilePathToConfig.put(configCat.getConfigFileName() + ".toml", configData);

    	configData.initData();
    	configData.writeConfigFile(false);
    }
    
    /* Get Inner Field value */
    public static Object getField(String configID, String name) {
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
    public static String getComment(String configID, String name) {    	
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
    public static boolean updateField(String configID, String name, Object obj) {
    	if (lookupRegistryNameToConfig.get(configID).setFieldBasedOnType(name, obj)) {
        	//writeHashMapsToFile();
            //invalid here now it just fully re-registers configs which is bad, we just force save outside here now
            //lookupRegistryNameToConfig.get(configID).writeConfigFile(true);
        	return true;
    	}
    	return false;
    }

    public static void forceSaveAllFilesFromRuntimeSettings() {
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

    public static void forceLoadRuntimeSettingsFromFile() {
        CULog.dbg("forceLoadRuntimeSettingsFromFile invoked");
        for (ModConfigData data : lookupRegistryNameToConfig.values()) {
            //data.reloadRuntimeFromFile();
            data.writeConfigFile(false);
        }
    }

    private static final LevelResource SERVERCONFIG = new LevelResource("serverconfig");

    private static Path getServerConfigPath(final MinecraftServer server)
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
