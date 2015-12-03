package CoroUtil.forge;

import modconfig.ConfigMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import CoroUtil.config.ConfigCoroAI;
import CoroUtil.diplomacy.TeamTypes;
import CoroUtil.pets.PetsManager;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.util.CoroUtilFile;
import CoroUtil.world.WorldDirectorManager;

@Mod(modid = "CoroAI", name="CoroAI", version="v1.0")
public class CoroAI {
	
	@Mod.Instance( value = "CoroAI" )
	public static CoroAI instance;
	public static String modID = "coroutil";
    
    
    @SidedProxy(clientSide = "CoroUtil.forge.ClientProxy", serverSide = "CoroUtil.forge.CommonProxy")
    public static CommonProxy proxy;
    
    public static boolean initProperNeededForInstance = true;
    
    public static String eventChannelName = "coroutil";
	public static final FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(eventChannelName);
    
    //public static PetsManager petsManager;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	ConfigMod.addConfigFile(event, "coroai", new ConfigCoroAI());
    	
    	eventChannel.register(new EventHandlerPacket());
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	//TickRegistry.registerTickHandler(new ServerTickHandler(this), Side.SERVER);
    	FMLCommonHandler.instance().bus().register(new EventHandlerFML());
    	MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
    	//MinecraftForge.EVENT_BUS.register(new EventHandler());
    	proxy.init(this);
    	TeamTypes.initTypes();

    	//petsManager = new PetsManager();
    }
    
    @Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
    	
	}
    
    public CoroAI() {
    	
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
