package CoroUtil.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;

import java.io.File;

public class ConfigDynamicDifficulty implements IConfigCategory {

	@ConfigComment("How long it takes to reach max difficulty level for a specific player in gameplay ticks (50 hours)")
	public static int difficulty_MaxTicksOnServer = 20*60*60*50;
	
	@ConfigComment("How long it takes to reach max difficulty level for a specific chunk in gameplay ticks (50 hours)")
	public static int difficulty_MaxTicksInChunk = 20*60*60*50;
	//public static int difficulty_MaxInventoryRating = 60;
	
	@ConfigComment("Distance from spawn required to hit the max difficulty for this setting")
	public static int difficulty_DistFromSpawnMax = 5000;
	/*@ConfigComment("How fast it increases difficulty to max distance")
	public static double difficulty_ScaleRate = 1D;*/

	@ConfigComment("How far around a player to lookup DPS info to average out a DPS calculation for local difficulty")
	public static int difficulty_BestDPSChunkRadius = 4;

	@ConfigComment("The expected best dps possible without mods, used to scale rating from 0 to 1")
	public static int difficulty_BestVanillaDPS = 20;

	@ConfigComment("The expected best health possible without mods, used to scale rating from 0 to 1")
	public static int difficulty_BestVanillaHealth = 20;

	@ConfigComment("The expected best armor possible without mods, used to scale rating from 0 to 1")
	public static int difficulty_BestVanillaArmor = 20;

	@ConfigComment("The expected best armor enchantment bonus possible without mods, used to scale rating from 0 to 1")
	public static int difficulty_BestVanillaArmorEnchant = 25;

	@ConfigComment("Enable to exclude things like passive mobs, cows, etc from being used to figure out DPS for a chunk")
	public static boolean difficulty_OnlyLogDPSToHostiles = false;

	@ConfigComment("Enable to only log things that can be tracked back to player, melee, bow usage, things like being on fire")
	public static boolean difficulty_OnlyLogDPSFromPlayerAsSource = false;

	@ConfigComment("Skip logging things like being in fire, being on fire, suffocation, fall damage. Lava damage will still be counted")
	public static boolean difficulty_DontLogDPSFromEnvironment = true;

	@ConfigComment("How much influence vanilla chunk inhabited time has on the averaged difficulty rating, higher number = more")
	public static double weightPosOccupy = 1D;

	@ConfigComment("How much influence player equipment rating has on the averaged difficulty rating, higher number = more")
	public static double weightPlayerEquipment = 1.5D;

	@ConfigComment("How much influence the players time in the game has on the averaged difficulty rating, higher number = more")
	public static double weightPlayerServerTime = 0D;

	@ConfigComment("How much influence a players calculated damage per second has on the averaged difficulty rating, higher number = more")
	public static double weightDPS = 1.5D;

	@ConfigComment("How much influence a players max health has on the averaged difficulty rating, higher number = more")
	public static double weightHealth = 1D;

	@ConfigComment("How much influence a players distance from spawn has on the averaged difficulty rating, higher number = more")
	public static double weightDistFromSpawn = 1D;

	@ConfigComment("How much influence a buffed location has on the averaged difficulty rating, higher number = more, currently unused")
	public static double weightBuffedLocation = 2D;

	@ConfigComment("How much influence debuffed location has on the averaged difficulty rating, higher number = more")
	public static double weightDebuffedLocation = 1D;
	
	@ConfigComment("unmodded difficulty is expected from 0 to 1, anything above 1 should be from mods, use this if you feel mods are making the difficulty way too high, -1 = dont cap it")
	public static double difficulty_Max = -1;

	@ConfigComment("How many game ticks until a repairing block fully restores to its original block")
	public static int ticksToRepairBlock = 20*60*5;

	@ConfigComment("For entities with block mining ability, how fast they mine a block per tick, higher is faster")
	public static double digSpeed = 0.01D;

	@ConfigComment("Prevents permanent damage caused by explosions during invasions, since zombie miners will be making holes they can get in")
	public static boolean convertExplodedBlocksToRepairingBlocksDuringInvasion = true;

	@ConfigComment("Chests, machines, etc, arent normal blocks that we can convert to repairing blocks, so instead this setting just protects them from being harmed at all by explosions")
	public static boolean preventExplodedTileEntitiesDuringInvasions = true;

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
