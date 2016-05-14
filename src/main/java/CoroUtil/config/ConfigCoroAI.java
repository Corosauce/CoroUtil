package CoroUtil.config;

import java.util.Arrays;

import CoroUtil.DimensionChunkCache;
import modconfig.ConfigComment;
import modconfig.IConfigCategory;

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
	
	@Override	
	public String getConfigFileName() {
		return "CoroUtil";
	}

	@Override
	public String getCategory() {
		return "CoroUtil";
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
