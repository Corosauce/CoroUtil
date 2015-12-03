package extendedrenderer;

import java.io.File;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import extendedrenderer.render.RotatingEffectRenderer;

@Mod(modid = "ExtendedRenderer", name="Extended Renderer", version="v1.0")
public class ExtendedRenderer {
	
	@Mod.Instance( value = "ExtendedRenderer" )
	public static ExtendedRenderer instance;
	public static String modid = "extendedrenderer";
    
    @SidedProxy(clientSide = "extendedrenderer.ClientProxy", serverSide = "extendedrenderer.CommonProxy")
    public static CommonProxy proxy;

    @SideOnly(Side.CLIENT)
    public static RotatingEffectRenderer rotEffRenderer;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	
    }
    
    @EventHandler
    public void load(FMLInitializationEvent event)
    {
    	proxy.init();
    	MinecraftForge.EVENT_BUS.register(new extendedrenderer.EventHandler());
    }
    
    @EventHandler
	public void postInit(FMLPostInitializationEvent event) {
    	proxy.postInit();
	}

    public ExtendedRenderer() {
    	
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
		return FMLClientHandler.instance().getClient().mcDataDir.getPath();
	}
	
	public static void dbg(Object obj) {
		if (true) System.out.println(obj);
	}
}
