package CoroUtil.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;

import java.io.File;

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

	public static int difficulty_BestVanillaHealth = 20;
	public static int difficulty_BestVanillaArmor = 20;
	public static int difficulty_BestVanillaArmorEnchant = 25;
	
	public static double difficulty_MaxDPSLoggable = 500;
	
	public static double difficulty_MaxDPSRatingAllowed = 5;

	public static double weightPosOccupy = 1D;
	public static double weightPlayerEquipment = 1.5D;
	public static double weightPlayerServerTime = 0D;
	public static double weightDPS = 1.5D;
	public static double weightHealth = 1D;
	public static double weightDistFromSpawn = 1D;
	public static double weightBuffedLocation = 2D;
	public static double weightDebuffedLocation = 1D;
	
	@ConfigComment("-1 = dont cap it")
	public static double difficulty_Max = -1;

	//TODO: if false, will we be double buffing infernal mobs accidentally?
	@ConfigComment("If true, tie overall chance of infernal mobs to our difficulty system scaling, if false, don't try to control it at all")
	public static boolean difficulty_OverrideInfernalMobs = true;
	
	/*@ConfigComment("what level of difficulty is required to count as 100% chance")
	public static double difficulty_Infernal_Elite_Max = 5F;
	@ConfigComment("what level of difficulty is required to count as 100% chance")
	public static double difficulty_Infernal_Ultra_Max = 5F;
	@ConfigComment("what level of difficulty is required to count as 100% chance")
	public static double difficulty_Infernal_Infernal_Max = 5F;
	
	public static double difficulty_Infernal_Elite_ScaleRate = 1F;
	public static double difficulty_Infernal_Ultra_ScaleRate = 1F;
	public static double difficulty_Infernal_Infernal_ScaleRate = 1F;*/

	@Override
	public String getName() {
		return "DynamicDifficulty";
	}

	@Override
	public String getRegistryName() {
		return "coroutildd";
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
		
	}

}
