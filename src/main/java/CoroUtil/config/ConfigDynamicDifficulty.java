package CoroUtil.config;

import java.util.Arrays;

import CoroUtil.DimensionChunkCache;
import modconfig.ConfigComment;
import modconfig.IConfigCategory;

public class ConfigDynamicDifficulty implements IConfigCategory {

	@ConfigComment("How long it takes to reach max difficulty level for a specific player in gameplay ticks (50 hours)")
	public static int difficulty_MaxTicksOnServer = 20*60*60*50;
	
	@ConfigComment("How long it takes to reach max difficulty level for a specific chunk in gameplay ticks (50 hours)")
	public static int difficulty_MaxTicksInChunk = 20*60*60*50;
	//public static int difficulty_MaxInventoryRating = 60;
	
	@ConfigComment("Track chunk bound data required for some difficulty calculations, disable if issues with server stability relating to CoroUtil")
	public static boolean trackChunkData = true;
	
	@Override
	public String getConfigFileName() {
		return "CoroUtil_DynamicDifficulty";
	}

	@Override
	public String getCategory() {
		return "CoroUtil_DynamicDifficulty";
	}

	@Override
	public void hookUpdatedValues() {
		
	}

}
