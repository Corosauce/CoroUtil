package CoroUtil.config;

import java.util.Arrays;

import CoroUtil.DimensionChunkCache;
import modconfig.ConfigComment;
import modconfig.IConfigCategory;

public class ConfigDynamicDifficulty implements IConfigCategory {

	@ConfigComment("Track chunk bound data required for some difficulty calculations, disable if issues with server stability relating to CoroUtil")
	public static boolean trackChunkData = true;
	
	@ConfigComment("How long it takes to reach max difficulty level for a specific player in gameplay ticks (50 hours)")
	public static int difficulty_MaxTicksOnServer = 20*60*60*50;
	
	@ConfigComment("How long it takes to reach max difficulty level for a specific chunk in gameplay ticks (50 hours)")
	public static int difficulty_MaxTicksInChunk = 20*60*60*50;
	//public static int difficulty_MaxInventoryRating = 60;
	
	@ConfigComment("Distance from spawn required to hit the max difficulty for this setting")
	public static int difficulty_DistFromSpawnMax = 5000;
	/*@ConfigComment("How fast it increases difficulty to max distance")
	public static double difficulty_ScaleRate = 1D;*/
	
	public static int difficulty_BestDPSRadius = 4;
	
	public static int difficulty_BestVanillaDPS = 20;
	
	public static double difficulty_MaxDPSLoggable = 500;
	
	public static double difficulty_MaxDPSRatingAllowed = 5;
	
	public static double weightPosOccupy = 1D;
	public static double weightPlayerEquipment = 1.5D;
	public static double weightPlayerServerTime = 1D;
	public static double weightDPS = 1.5D;
	public static double weightHealth = 1D;
	public static double weightDistFromSpawn = 1D;
	
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
