package CoroUtil.config;

import java.io.File;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;

public class ConfigHWMonsters implements IConfigCategory {

	@ConfigComment("Flying is overpowered!")
	public static boolean antiAir = true;
	
	@ConfigComment("0 = leap and pull down, 1 = super evil effects and force pull down")
	public static int antiAirType = 1;
	
	public static double antiAirLeapSpeed = 0.15D;
	
	public static double antiAirPullDownRate = -0.4D;
	public static boolean antiAirUseRelativeMotion = true;
	
	public static boolean antiAirApplyPotions = true;
	
	public static int antiAirLeapRate = 40;
	
	public static int antiAirTryDist = 20;/*
	
	@ConfigComment("Additional scaling for dynamic difficulty based health boost")
	public static double scaleHealth = 1D;

	public static double scaleHealthMax = 0.5D;
	
	//public static double scaleSpeed = 1D;

	@ConfigComment("Maximum speed buff allowed for max difficulty, scales based on current difficulty")
	public static double scaleSpeed = 1.3;
	
	public static double scaleSpeedCap = 0.5;
	
	@ConfigComment("0 to disable")
	public static double scaleKnockbackResistance = 1D;
	
	@ConfigComment("0 to disable")
	public static double scaleLeapAttackUseChance = 1D;
	
	@ConfigComment("0 to disable")
	public static double scaleLungeUseChance = 1D;*/
	
	public static double lungeDist = 7D;
	public static double lungeSpeed = 0.3D;
	public static double speedTowardsTargetLunge = 1.3D;
	public static long counterAttackDetectThreshold = 15;
	public static long counterAttackReuseDelay = 30;
	public static double counterAttackLeapSpeed = 0.8D;
	public static double counterAttackLeapExtraDamageMultiplier = 0.5D;
	public static boolean counterAttackLeapArmorPiercing = false;
	
	public static String blackListPlayers = "";
	public static boolean useBlacklistAsWhitelist = false;

	public static boolean genTotems = true;

	@ConfigComment("This will also do what explosionsDontDestroyTileEntities does since I cant convert those to repairing blocks")
	public static boolean explosionsTurnIntoRepairingBlocks = false;

	@ConfigComment("This will protect things like chests, furnaces, any more complex blocks that have inventories etc")
	public static boolean explosionsDontDestroyTileEntities = false;


	@Override
	public String getName() {
		return "Misc";
	}

	@Override
	public String getRegistryName() {
		return "HWMonstersMisc";
	}
	
	@Override
	public String getConfigFileName() {
		return "HW_Monsters" + File.separator + getName();
	}

	@Override
	public String getCategory() {
		return "HW-M-Misc";
	}

	@Override
	public void hookUpdatedValues() {
		
	}

}
