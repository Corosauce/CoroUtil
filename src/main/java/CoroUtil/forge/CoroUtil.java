package CoroUtil.forge;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.difficulty.data.DifficultyDataReader;
import modconfig.ConfigMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
import CoroUtil.config.ConfigCoroAI;
import CoroUtil.config.ConfigDynamicDifficulty;
import CoroUtil.diplomacy.TeamTypes;
import CoroUtil.pets.PetsManager;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.util.CoroUtilFile;
import CoroUtil.world.WorldDirectorManager;

import com.google.common.collect.BiMap;

@Mod(modid = "coroutil", name="coroutil", version=CoroUtil.version, acceptableRemoteVersions="*")
public class CoroUtil {
	
	@Mod.Instance( value = "coroutil" )
	public static CoroUtil instance;
	public static String modID = "coroutil";

	public static final String version = "${version}";
    
    @SidedProxy(clientSide = "CoroUtil.forge.ClientProxy", serverSide = "CoroUtil.forge.CommonProxy")
    public static CommonProxy proxy;
    
    public static boolean initProperNeededForInstance = true;
    
    public static String eventChannelName = "coroutil";
	public static final FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(eventChannelName);
    
    //public static PetsManager petsManager;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	ConfigMod.addConfigFile(event, new ConfigCoroAI());
    	ConfigMod.addConfigFile(event, new ConfigDynamicDifficulty());
		ConfigMod.addConfigFile(event, new ConfigHWMonsters());

		DifficultyDataReader reader = new DifficultyDataReader();
		reader.loadFiles();
    	
    	eventChannel.register(new EventHandlerPacket());
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
	    		Iterator<Map.Entry<String, Class<? extends Entity>>> it2 = EntityList.NAME_TO_CLASS.entrySet().iterator();
	    		while (it2.hasNext()) {
	    			Entry<String, Class<? extends Entity>> entReg = it2.next();
	    			
	    			System.out.println(entReg.getKey() + " - " + entReg.getValue());
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
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
    		System.out.println("CoroUtil being reinitialized");
    		initProperNeededForInstance = false;
	    	CoroUtilFile.getWorldFolderName();
	    	PetsManager.instance().nbtReadFromDisk();
	    	
	    	//dont read in world director manager stuff, its loaded on demand per registration, for directors and grids
	    	WorldDirectorManager.instance().reset();
    	}
    }
    
    public static void writeOutData(boolean unloadInstances) {
    	try {
    		PetsManager.instance().nbtWriteToDisk();
	    	if (unloadInstances) PetsManager.instance().reset();
	    	PlayerQuestManager.i().saveData(false, unloadInstances);
	    	WorldDirectorManager.instance().writeToFile(unloadInstances);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
	public static void dbg(Object obj) {
		if (true) {
			//MinecraftServer.getServer().getLogAgent().logInfo(String.valueOf(obj));
			System.out.println(obj);
		}
	}
}
