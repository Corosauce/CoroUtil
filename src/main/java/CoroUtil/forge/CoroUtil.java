package CoroUtil.forge;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import CoroUtil.config.ConfigCoroUtilAdvanced;
import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.difficulty.data.DifficultyDataReader;
import CoroUtil.util.CoroUtilCompatibility;
import CoroUtil.util.CoroUtilMisc;
import modconfig.ConfigMod;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;
import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.config.ConfigDynamicDifficulty;
import CoroUtil.diplomacy.TeamTypes;
import CoroUtil.pets.PetsManager;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.util.CoroUtilFile;
import CoroUtil.world.WorldDirectorManager;

import com.google.common.collect.BiMap;

@Mod(modid = CoroUtil.modID, name=CoroUtil.modID, version=CoroUtil.version, acceptableRemoteVersions="*")
public class CoroUtil {
	
	@Mod.Instance( value = CoroUtil.modID )
	public static CoroUtil instance;
	public static final String modID = "coroutil";

	public static final String modID_HWMonsters = "hw_monsters";
	public static final String modID_HWInvasions = "hw_inv";


	//public static final String version = "${version}";
	//when we definitely need to enforce a new CoroUtil version outside dev, use this for production
	//TODO: find a way to perminently do this for dev only
	public static final String version = "1.12.1-1.2.25";
    
    @SidedProxy(clientSide = "CoroUtil.forge.ClientProxy", serverSide = "CoroUtil.forge.CommonProxy")
    public static CommonProxy proxy;
    
    public static boolean initProperNeededForInstance = true;
    
    public static String eventChannelName = "coroutil";
	public static final FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(eventChannelName);
    
    public static ConfigCoroUtil configCoroUtil = null;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
		migrateOldConfig();

		configCoroUtil = new ConfigCoroUtil();
    	ConfigMod.addConfigFile(event, configCoroUtil);
		if (ConfigCoroUtil.enableAdvancedDeveloperConfigFiles || CoroUtilCompatibility.isHWInvasionsInstalled() || CoroUtilCompatibility.isHWMonstersInstalled()) {
			ConfigMod.addConfigFile(event, new ConfigDynamicDifficulty());
		}
    	if (ConfigCoroUtil.enableAdvancedDeveloperConfigFiles) {
			ConfigMod.addConfigFile(event, new ConfigHWMonsters());
			ConfigMod.addConfigFile(event, new ConfigCoroUtilAdvanced());
		}

		/*DifficultyDataReader reader = new DifficultyDataReader();
		reader.loadFiles();*/
		DifficultyDataReader.init();
		DifficultyDataReader.loadFiles();
    	
    	eventChannel.register(new EventHandlerPacket());
    }

    public static void migrateOldConfig() {

    	File path = new File("." + File.separator + "config" + File.separator + "CoroUtil");
    	try {
			path.mkdirs();
		} catch (Exception ex) {
    		ex.printStackTrace();
		}

		File oldFile = new File("." + File.separator + "config" + File.separator + "CoroUtil.cfg");
		File newFile = new File("." + File.separator + "config" + File.separator + "CoroUtil" + File.separator + "General.cfg");

		fixConfigFile(oldFile, newFile, "coroutil {", "general {");

		oldFile = new File("." + File.separator + "config" + File.separator + "CoroUtil_DynamicDifficulty.cfg");
		newFile = new File("." + File.separator + "config" + File.separator + "CoroUtil" + File.separator + "DynamicDifficulty.cfg");

		fixConfigFile(oldFile, newFile, "coroutil_dynamicdifficulty {", "dynamicdifficulty {");
	}

	public static void fixConfigFile(File oldFile, File newFile, String oldCat, String newCat) {
		if (oldFile.exists() && !newFile.exists()) {
			CULog.log("Detected old " + oldFile.toString() + ", relocating to " + newFile.toString());
			try {
				Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

				//fix category
				List<String> newLines = new ArrayList<>();
				for (String line : Files.readAllLines(newFile.toPath(), StandardCharsets.UTF_8)) {
					if (line.contains(oldCat)) {
						newLines.add(line.replace(oldCat, newCat));
					} else {
						newLines.add(line);
					}
				}
				Files.write(newFile.toPath(), newLines, StandardCharsets.UTF_8);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	//TickRegistry.registerTickHandler(new ServerTickHandler(this), Side.SERVER);
		MinecraftForge.EVENT_BUS.register(new EventHandlerFML());
    	MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
    	//MinecraftForge.EVENT_BUS.register(new EventHandler());
    	proxy.init(this);
    	TeamTypes.initTypes();

    	//petsManager = new PetsManager();
    }
    
    @Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
    	boolean debugOutputEntityRegistrations = false;
    	if (debugOutputEntityRegistrations) {
	    	try {
	    		BiMap<Class<? extends Entity>, EntityRegistration> entityClassRegistrations = ObfuscationReflectionHelper.getPrivateValue(EntityRegistry.class, EntityRegistry.instance(), "entityClassRegistrations");
	    		
	    		Iterator<EntityRegistration> it = entityClassRegistrations.values().iterator();
	    		while (it.hasNext()) {
	    			EntityRegistration entReg = it.next();
	    			
	    			//System.out.println(entReg.getEntityName());
	    			
	    			
	    		}
	    		
	    		//Iterator<Class<? extends Entity>> it2 = EntityList.stringToClassMapping.values().iterator();
	    		/*Iterator<Map.Entry<String, Class<? extends Entity>>> it2 = EntityList.NAME_TO_CLASS.entrySet().iterator();
	    		while (it2.hasNext()) {
	    			Entry<String, Class<? extends Entity>> entReg = it2.next();
	    			
	    			System.out.println(entReg.getKey() + " - " + entReg.getValue());
	    		}*/
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}

    	if (ConfigCoroUtilAdvanced.fixBadBiomeEntitySpawnEntries) {

            CULog.log("fixBadBiomeEntitySpawnEntries enabled, scanning and fixing all biome entity spawn lists for potential crash risks");

            CoroUtilMisc.fixBadBiomeEntitySpawns();
        }
	}

	public void postInit() {
    	proxy.postInit();
	}
    
    public CoroUtil() {
    	
    }
    
    @Mod.EventHandler
    public void serverStart(FMLServerStartedEvent event) {
    	
    }
    
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    	event.registerServerCommand(new CommandCoroUtil());
    }
    
    @Mod.EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
    	writeOutData(true);
    	
    	initProperNeededForInstance = true;
    }
    
    public static void initTry() {
    	if (initProperNeededForInstance) {
    		CULog.log("CoroUtil being reinitialized");
    		initProperNeededForInstance = false;
	    	CoroUtilFile.getWorldFolderName();
	    	//dont prevent reading in if ConfigCoroUtil.useCoroPets is false,
			//so they can have the data if they enable it while server running
	    	PetsManager.instance().nbtReadFromDisk();
	    	
	    	//dont read in world director manager stuff, its loaded on demand per registration, for directors and grids
	    	WorldDirectorManager.instance().reset();
    	}
    }
    
    public static void writeOutData(boolean unloadInstances) {
    	try {
    		if (ConfigCoroUtilAdvanced.useCoroPets) {
				PetsManager.instance().nbtWriteToDisk();
				if (unloadInstances) PetsManager.instance().reset();
			}
	    	PlayerQuestManager.i().saveData(false, unloadInstances);
	    	WorldDirectorManager.instance().writeToFile(unloadInstances);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
	public static void dbg(String obj) {
    	CULog.dbg(obj);
	}
}
