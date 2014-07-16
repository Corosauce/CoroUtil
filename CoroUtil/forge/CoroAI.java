package CoroUtil.forge;

import modconfig.ConfigMod;
import net.minecraftforge.common.MinecraftForge;
import CoroUtil.config.ConfigCoroAI;
import CoroUtil.diplomacy.TeamTypes;
import CoroUtil.pets.PetsManager;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.util.CoroUtilFile;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "CoroAI", name="CoroAI", version="v1.0")
public class CoroAI {
	
	@Mod.Instance( value = "CoroAI" )
	public static CoroAI instance;
    
    
    @SidedProxy(clientSide = "CoroUtil.forge.ClientProxy", serverSide = "CoroUtil.forge.CommonProxy")
    public static CommonProxy proxy;
    
    public static boolean initProperNeededForInstance = true;
    
    public static String eventChannelName = "coroutil";
	public static final FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(eventChannelName);
    
    public static PetsManager petsManager;
    
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

    	petsManager = new PetsManager();
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
	    	petsManager.nbtReadFromDisk();
    	}
    }
    
    public static void writeOutData(boolean unloadInstances) {
    	petsManager.nbtWriteToDisk();
    	if (unloadInstances) petsManager.reset();
    	PlayerQuestManager.i().saveData(false, unloadInstances);
    }
    
	public static void dbg(Object obj) {
		if (true) {
			//MinecraftServer.getServer().getLogAgent().logInfo(String.valueOf(obj));
			System.out.println(obj);
		}
	}
}
