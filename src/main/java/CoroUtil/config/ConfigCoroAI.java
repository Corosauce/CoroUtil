package CoroUtil.config;

import java.util.Arrays;

import CoroUtil.DimensionChunkCache;
import modconfig.IConfigCategory;

public class ConfigCoroAI implements IConfigCategory {

	public static boolean chunkCacheOverworldOnly = false;
	public static boolean usePlayerRadiusChunkLoadingForFallback = true;
	
	public static String chunkCacheDimensionBlacklist_IDs = "";
	public static String chunkCacheDimensionBlacklist_Names = "promised";
	
	public static boolean trackPlayerData = false;
	
	public static boolean useBlackListsAsWhitelist = false;
	
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
			
			DimensionChunkCache.listBlacklistIDs.clear();
			for (int i = 0; i < ids.length; i++) {
				DimensionChunkCache.listBlacklistIDs.add(Integer.valueOf(ids[i]));
			}
			DimensionChunkCache.listBlacklistNamess = Arrays.asList(names);
		} catch (Exception ex) {
			//silence!
		}
	}

}
