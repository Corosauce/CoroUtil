package CoroPets;

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
import CoroUtil.util.CoroUtilFile;

@Mod(modid = "CoroPets", name="CoroPets", version="v1.0")
public class CoroPets {
	
	@Mod.Instance( value = "CoroPets" )
	public static CoroPets instance;
    
    @SidedProxy(clientSide = "CoroPets.ClientProxy", serverSide = "CoroPets.CommonProxy")
    public static CommonProxy proxy;
    
    public static boolean initProperNeededForInstance = true;
    
    public static String eventChannelName = "coropets";
	public static final FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(eventChannelName);
	
	public static String tameString = "CoroPets_isTame";
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	//ConfigMod.addConfigFile(event, "coropets", new ConfigCoroAI());
    	
    	eventChannel.register(new EventHandlerPacket());
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	//TickRegistry.registerTickHandler(new ServerTickHandler(this), Side.SERVER);
    	FMLCommonHandler.instance().bus().register(new EventHandlerFML());
    	MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
    	//MinecraftForge.EVENT_BUS.register(new EventHandler());
    	proxy.init();
    }
    
    @Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
    	
	}
    
    public CoroPets() {
    	
    }
    
    @Mod.EventHandler
    public void serverStart(FMLServerStartedEvent event) {
    	
    }
    
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    	event.registerServerCommand(new CommandCoroPets());
    }
    
    @Mod.EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
    	writeOutData(true);
    	
    	initProperNeededForInstance = true;
    }
    
    public static void initTry() {
    	if (initProperNeededForInstance) {
    		//System.out.println("CoroUtil being reinitialized");
    		initProperNeededForInstance = false;
	    	CoroUtilFile.getWorldFolderName();
    	}
    }
    
    public static void writeOutData(boolean unloadInstances) {
    	//PlayerQuestManager.i().saveData(false, unloadInstances);
    }
    
	public static void dbg(Object obj) {
		if (true) {
			//MinecraftServer.getServer().getLogAgent().logInfo(String.valueOf(obj));
			System.out.println(obj);
		}
	}
}
