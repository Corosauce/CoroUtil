package CoroUtil.forge;

import modconfig.ConfigMod;
import net.minecraftforge.common.MinecraftForge;
import CoroUtil.config.ConfigCoroAI;
import CoroUtil.diplomacy.TeamTypes;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.util.CoroUtilFile;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@NetworkMod(channels = { "CoroUtilQuest", "CoroAI_Inv", "CoroAI_TEntCmd", "CoroAI_TEntDW", "CoroAI_Ent", "NBTData_CONT", "NBTData_GUI" }, clientSideRequired = true, serverSideRequired = false, packetHandler = CoroAIPacketHandler.class)
@Mod(modid = "CoroAI", name="CoroAI", version="v1.0")
public class CoroAI {
	
	@Mod.Instance( value = "CoroAI" )
	public static CoroAI instance;
    
    
    @SidedProxy(clientSide = "CoroUtil.forge.ClientProxy", serverSide = "CoroUtil.forge.CommonProxy")
    public static CommonProxy proxy;
    
    public static boolean initProperNeededForInstance = true;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	ConfigMod.addConfigFile(event, "coroai", new ConfigCoroAI());
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	TickRegistry.registerTickHandler(new ServerTickHandler(this), Side.SERVER);
    	MinecraftForge.EVENT_BUS.register(new CoroUtilEventHandler());
    	//MinecraftForge.EVENT_BUS.register(new EventHandler());
    	proxy.init(this);
    	TeamTypes.initTypes();
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
    	}
    }
    
    public static void writeOutData(boolean unloadInstances) {
    	PlayerQuestManager.i().saveData(false, unloadInstances);
    }
    
	public static void dbg(Object obj) {
		if (true) {
			//MinecraftServer.getServer().getLogAgent().logInfo(String.valueOf(obj));
			System.out.println(obj);
		}
	}
}
