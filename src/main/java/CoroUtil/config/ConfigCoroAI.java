package CoroUtil.config;

import java.util.Arrays;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import CoroUtil.util.DimensionChunkCacheNew;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConfigCoroAI implements IConfigCategory {

	public static boolean chunkCacheOverworldOnly = false;
	public static boolean usePlayerRadiusChunkLoadingForFallback = true;
	
	public static String chunkCacheDimensionBlacklist_IDs = "";
	public static String chunkCacheDimensionBlacklist_Names = "promised";
	
	public static boolean trackPlayerData = false;
	
	public static boolean useBlackListsAsWhitelist = false;
	
	public static boolean PFQueueDebug = false;
	
	@ConfigComment("Test admin thing for kcauldron issues, kills zombies a bit after sunrise every cleanupStrayMobsDayRate days")
	public static boolean cleanupStrayMobs = false;
	
	public static int cleanupStrayMobsDayRate = 5;
	
	public static int cleanupStrayMobsTimeOfDay = 2000;
	
	public static boolean desirePathDerp = false;
	
	public static boolean headshots = false;

	public static boolean disableParticleRenderer = false;
	public static boolean disableMipmapFix = false;

	public static boolean forceShadersOff = false;

	@ConfigComment("Provides better context for shaders/particles to work nice with translucent blocks like glass and water")
	public static boolean useEntityRenderHookForShaders = true;

	//maybe temp
	@ConfigComment("WIP, more strict transparent cloud usage, better on fps")
	public static boolean optimizedCloudRendering = false;

	public static boolean debugShaders = false;

	public static boolean foliageShaders = true;
	public static boolean particleShaders = true;
	public static boolean useLoggingLog = true;
	public static boolean useLoggingDebug = false;
	public static boolean useLoggingError = true;

	@Override
	public String getName() {
		return "CoroUtil";
	}

	@Override
	public String getRegistryName() {
		return "coroai";
	}

	@Override
	public String getConfigFileName() {
		return getName();
	}

	@Override
	public String getCategory() {
		return getName();
	}

	@Override
	public void hookUpdatedValues() {
		try {
			String[] ids = chunkCacheDimensionBlacklist_IDs.split(",");
			String[] names = chunkCacheDimensionBlacklist_Names.split(",");
			
			DimensionChunkCacheNew.listBlacklistIDs.clear();
			for (int i = 0; i < ids.length; i++) {
				DimensionChunkCacheNew.listBlacklistIDs.add(Integer.valueOf(ids[i]));
			}
			DimensionChunkCacheNew.listBlacklistNamess = Arrays.asList(names);
		} catch (Exception ex) {
			//silence!
		}
	}

}
