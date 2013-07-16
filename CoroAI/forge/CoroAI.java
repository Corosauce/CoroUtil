package CoroAI.forge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import CoroAI.diplomacy.TeamTypes;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@NetworkMod(channels = { "CoroAI_Inv", "CoroAI_TEntCmd" }, clientSideRequired = true, serverSideRequired = false, packetHandler = CoroAIPacketHandler.class)
@Mod(modid = "CoroAI", name="CoroAI", version="v1.0")
public class CoroAI {
	
	@Mod.Instance( value = "CoroAI" )
	public static CoroAI instance;
    
    
    @SidedProxy(clientSide = "CoroAI.forge.ClientProxy", serverSide = "CoroAI.forge.CommonProxy")
    public static CommonProxy proxy;
    
    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
    	//ConfigMod.addConfigFile(event, "enhancedcombat", new ModConfigFields());
    }
    
    @Init
    public void load(FMLInitializationEvent event)
    {
    	TickRegistry.registerTickHandler(new ServerTickHandler(this), Side.SERVER);
    	//MinecraftForge.EVENT_BUS.register(new EventHandler());
    	proxy.init(this);
    	TeamTypes.initTypes();
    }
    
    @PostInit
	public void postInit(FMLPostInitializationEvent event) {
    	
	}
    
    public CoroAI() {
    	
    }
    
    @Mod.ServerStarted
    public void serverStart(FMLServerStartedEvent event) {
    	
    }
    
    @Mod.ServerStopped
    public void serverStop(FMLServerStoppedEvent event) {
    	
    }
    
    public static String lastWorldFolder = "";
    
    public static String getWorldFolderName() {
		World world = DimensionManager.getWorld(0);
		
		if (world != null) {
			lastWorldFolder = ((WorldServer)world).getChunkSaveLocation().getName();
			return lastWorldFolder + File.separator;
		}
		
		return lastWorldFolder + File.separator;
	}
	
	public static String getSaveFolderPath() {
    	if (MinecraftServer.getServer() == null || MinecraftServer.getServer().isSinglePlayer()) {
    		return getClientSidePath() + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    	
    }
	
	public static String getWorldSaveFolderPath() {
    	if (MinecraftServer.getServer() == null || MinecraftServer.getServer().isSinglePlayer()) {
    		return getClientSidePath() + File.separator + "saves" + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    	
    }
    
    @SideOnly(Side.CLIENT)
	public static String getClientSidePath() {
		return FMLClientHandler.instance().getClient().getMinecraftDir().getPath();
	}
    
	public static void dbg(Object obj) {
		if (true) {
			//MinecraftServer.getServer().getLogAgent().logInfo(String.valueOf(obj));
			System.out.println(obj);
		}
	}
}
