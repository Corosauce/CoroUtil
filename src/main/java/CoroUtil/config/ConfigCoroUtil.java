package CoroUtil.config;

import java.io.File;
import java.util.Arrays;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import CoroUtil.util.DimensionChunkCacheNew;

public class ConfigCoroUtil implements IConfigCategory {

	public static boolean chunkCacheOverworldOnly = false;
	public static boolean usePlayerRadiusChunkLoadingForFallback = true;
	
	public static String chunkCacheDimensionBlacklist_IDs = "";
	public static String chunkCacheDimensionBlacklist_Names = "promised";

	@ConfigComment("Used for tracking time spent in chunk and block right clicks for measuring activity for difficulty")
	public static boolean trackPlayerData = false;
	
	//public static boolean useBlackListsAsWhitelist = false;
	
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

	public static boolean foliageShaders = false;
	public static boolean particleShaders = true;

	@ConfigComment("For seldom used but important things to print out in production")
	public static boolean useLoggingLog = true;

	@ConfigComment("For debugging things")
	public static boolean useLoggingDebug = false;

	@ConfigComment("For logging warnings/errors")
	public static boolean useLoggingError = true;

	public static boolean useCoroPets = false;

	@ConfigComment("Fix WorldEntitySpawner crash caused by other mods that look like this https://github.com/pWn3d1337/Techguns/issues/132")
	public static boolean fixBadBiomeEntitySpawnEntries = false;

	@ConfigComment("Use at own risk, will not support, requires game restart on change")
	public static boolean enableAdvancedDeveloperConfigFiles = false;

	@Override
	public String getName() {
		return "General";
	}

	@Override
	public String getRegistryName() {
		return "coroutil_general";
	}

	@Override
	public String getConfigFileName() {
		return "CoroUtil" + File.separator + getName();
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
