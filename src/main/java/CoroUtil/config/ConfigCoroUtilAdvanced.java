package CoroUtil.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;

import java.io.File;

public class ConfigCoroUtilAdvanced implements IConfigCategory {

	public static String mobSpawnsProfile = "mob_spawns";

	@ConfigComment("Force a specific profile to spawn, will ignore conditions and force it too, usefull for testing to see how a custom invasion will play out in normal circumstances")
	public static String mobSpawnsWaveToForceUse = "";

	@ConfigComment("max repair speed will be whatever scheduleBlockUpdate set the update, which is 30 seconds")
	public static boolean repairBlockNextRandomTick = false;

	@ConfigComment("Probably usefull if you want zombie miners get stopped by FTBU claimed chunks for example, but i dont want this behavior by default")
	public static boolean blockBreakingInvokesCancellableEvent = false;

	public static boolean removeInvasionAIWhenInvasionDone = true;

	//TODO: if false, will we be double buffing infernal mobs accidentally?
	@ConfigComment("Only used of HWMonsters is installed. If true, tie overall chance of infernal mobs to our difficulty system scaling, if false, don't try to control it at all")
	public static boolean difficulty_OverrideInfernalMobs = true;

	@ConfigComment("Track chunk bound data required for some difficulty calculations, disable if issues with server stability relating to CoroUtil, will affect HW-Invasions")
	public static boolean trackChunkData = true;

	@ConfigComment("-1 to disable. Not counting instant hits, this is a workaround for an ongoing issue where extremely high hit rates are logged causing super high dps")
	public static double difficulty_MaxAttackSpeedLoggable = 10;

	//might be conflicting with difficulty_MaxDPSLoggable
	public static double difficulty_MaxDPSRatingAllowed = 5;

	public static double difficulty_MaxDPSLoggable = 500;

	@Override
	public String getName() {
		return "Advanced";
	}

	@Override
	public String getRegistryName() {
		return "coroutil_advanced";
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
