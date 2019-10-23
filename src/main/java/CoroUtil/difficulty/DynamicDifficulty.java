package CoroUtil.difficulty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import CoroUtil.config.ConfigCoroUtilAdvanced;
import CoroUtil.forge.CULog;
import CoroUtil.util.CoroUtilWorldTime;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.location.ISimulationTickable;
import net.minecraft.block.*;
import net.minecraft.block.LogBlock;
import net.minecraft.block.OreBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.oredict.OreDictionary;
import CoroUtil.config.ConfigDynamicDifficulty;
import CoroUtil.entity.data.AttackData;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.UtilPlayer;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.grid.chunk.ChunkDataPoint;

/**
 * Design notes:
 *
 * - Difficulty for vanillaish ranges from 0 to 1
 * - 0 = just starting
 * - 1 = Typical endgame expected scenario, eg best equipment, 50 hours occupying base
 *
 * - It can go past 1 for vanilla though with:
 * -- occupied chunk time
 * -- server time (default off)
 * -- dist from spawn
 * -- dps if they somehow exceed what i consider best vanilla dps possible
 *
 * - mod focused buffs
 * -- max health (tinkers buffs)
 *
 */
public class DynamicDifficulty {
	
	public static String dataPlayerServerTicks = "HW_dataPlayerServerTicks";
	public static String dataPlayerLastCacheEquipmentRating = "HW_dataPlayerLastCacheEquipmentRating";
	public static String dataPlayerHarvestOre = "HW_dataPlayerHarvestOre";
	public static String dataPlayerHarvestLog = "HW_dataPlayerHarvestLog";
	public static String dataPlayerHarvestRating = "HW_dataPlayerHarvestRating";
	public static String dataPlayerDetectInAirTime = "HW_dataPlayerDetectInAirTime";

	public static String dataPlayerInvasionSkipping = "HW_dataPlayerInvasionSkipping";
	public static String dataPlayerInvasionSkipCount = "HW_dataPlayerInvasionSkipCount";
    public static String dataPlayerInvasionSkipCountForMultiplier = "HW_dataPlayerInvasionSkipCountForMultiplier";

	public static String dataPlayerInvasionSkippingTooSoon = "HW_dataPlayerInvasionSkippingTooSoon";

	public static String dataPlayerInvasionSkipBuff = "HW_dataPlayerInvasionSkipBuff";
	
	private static int tickRate = 20;
	
	public static HashMap<Integer, AttackData> lookupEntToDamageLog = new HashMap<Integer, AttackData>();

	public static List<String> listBlacklistedDamageSources = new ArrayList<>();

	public static void tickServer(ServerTickEvent event) {
		World world = DimensionManager.getWorld(0);
		if (world != null) {
			for (Object player : world.playerEntities) {
				if (player instanceof PlayerEntity) {
					tickPlayer((PlayerEntity)player);
				}
			}
			
			
			if (ConfigCoroUtilAdvanced.cleanupStrayMobs) {
				long dayNumber = (world.getDayTime() / CoroUtilWorldTime.getDayLength()) + 1;
				if (dayNumber % ConfigCoroUtilAdvanced.cleanupStrayMobsDayRate == 0) {
					long timeOfDay = world.getDayTime() % CoroUtilWorldTime.getDayLength();
					int killTimeRange = 10;
					if (timeOfDay >= (long) ConfigCoroUtilAdvanced.cleanupStrayMobsTimeOfDay && timeOfDay < (long)(2000+killTimeRange)) {
						CULog.dbg("KILLING ALL ZOMBIES!");
						for (Object obj : world.loadedEntityList) {
							if (obj instanceof ZombieEntity) {
								((ZombieEntity) obj).remove();
							}
						}
					}
				}
			}
		}
	}
	
	public static void tickPlayer(PlayerEntity player) {
		World world = player.world;

		if (!player.isSpectator() && !player.isCreative()) {
			if (world.getGameTime() % tickRate == 0) {
				long ticksPlayed = player.getPersistentData().getLong(dataPlayerServerTicks);
				ticksPlayed += tickRate;
				player.getPersistentData().putLong(dataPlayerServerTicks, ticksPlayed);
			}
		}

		int wallScanRange = 3;

		boolean autoAttackTest = true;
		boolean isInAir = false;
		
		boolean dbg = false;
		
		if (dbg) System.out.println("player tick");
		
		if ((!player.abilities.isCreativeMode || autoAttackTest)) {
			if (dbg) System.out.println("1");
    		if ((player.abilities.isFlying || (!player.onGround && !player.isInWater() && !player.isInsideOfMaterial(Material.LAVA)))) {
    			if (dbg) System.out.println("2");
    			if (player.getRidingEntity() == null) {
    				if (dbg) System.out.println("3");
    				Block block = null;
    				int pX = MathHelper.floor(player.posX);
    				int pY = MathHelper.floor(player.getBoundingBox().minY);
    				int pZ = MathHelper.floor(player.posZ);
    				boolean foundWall = false;
    				for (int x = -wallScanRange; !foundWall && x <= wallScanRange; x++) {
    					for (int z = -wallScanRange; !foundWall && z <= wallScanRange; z++) {
    						for (int y = -wallScanRange; !foundWall && y <= wallScanRange; y++) {
								BlockPos pos = new BlockPos(pX+x, pY+y, pZ+z);
    							BlockState state = world.getBlockState(pos);
    							block = state.getBlock();
    							if (!block.isAir(state, world, pos)) {
    								List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
    								block.addCollisionBoxToList(state, world, new BlockPos(pX+x, pY+y, pZ+z), player.getBoundingBox(), list, player, true);
    								if (list.size() > 0) {
    									if (dbg) System.out.println("wall found - " + block + " - " + (pX+x) + ", " + (pY+y) + ", " + (pZ+z));
        								foundWall = true;
        								break;
    								}
    								
    							}
    						}
    						
    					}
    				}
    				
    				if (!foundWall) {
    					if (dbg) System.out.println("no wall found");
    					isInAir = true;
    				}
    			}
    		}
		}
		
		if (isInAir) {
			if (dbg) System.out.println("in air");
			long airTime = player.getPersistentData().getLong(dataPlayerDetectInAirTime);
			player.getPersistentData().putLong(dataPlayerDetectInAirTime, airTime+1);
		} else {
			if (dbg) System.out.println("not in air");
			player.getPersistentData().putLong(dataPlayerDetectInAirTime, 0);
		}
		
	}

	public static void deathPlayer(PlayerEntity player) {
		//TODO: find existing ones and reset their timer? what about it not buffing the exact new spot
		//worth noting, this wont adjust existing entities in the area, only new ones
		//so they will still have strong enemies hanging around their death spot if its for an invasion

		WorldDirector wd = WorldDirectorManager.instance().getCoroUtilWorldDirector(player.world);

		if (wd != null) {
			BuffedLocation debuff = new BuffedLocation(32, -2);
			debuff.setDecays(true);
			debuff.setWorldID(player.world.provider.getDimension());
			debuff.setOrigin(new BlockCoord(player.getPosition()));
			wd.addTickingLocation(debuff);
		}
	}

	public static float getDifficultyAveragedForArea(CreatureEntity spawnedEntity) {
		return getDifficultyAveragedForArea(spawnedEntity, spawnedEntity.getPosition());
	}

	public static float getDifficultyAveragedForArea(CreatureEntity spawnedEntity, BlockPos pos) {
		return getDifficultyAveragedForArea(spawnedEntity, pos.getX(), pos.getY(), pos.getZ());
	}

	public static float getDifficultyAveragedForArea(CreatureEntity spawnedEntity, int x, int y, int z) {

		//TODO: cache so there is less player lookup thrashing for spawn candidate code
		PlayerEntity player = spawnedEntity.world.getClosestPlayerToEntity(spawnedEntity, -1);

		if (player != null) {
			return getDifficultyScaleAverage(player, x, y, z);
		}

		return 0;
	}
	
	public static float getDifficultyScaleAverage(PlayerEntity player, int x, int y, int z) {
		return getDifficultyScaleAverage(player, new BlockCoord(x, y, z));
	}
	
	public static float getDifficultyScaleAverage(World world, PlayerEntity player, BlockCoord pos) {
		return getDifficultyScaleAverage(player, pos);
	}
	
	public static float getDifficultyScaleAverage(PlayerEntity player, BlockCoord pos) {
		
		World world = player.world;
		
		//difficulties designed for stuff only mods are capable of should be a flat out plus to the rating such as:
		//- max health
		//- ???

		//TODO: DEBUFF!
		//consider having no weight for it, or only using it when its != 0

		float weightPosOccupy = (float) ConfigDynamicDifficulty.weightPosOccupy;
		float weightPlayerEquipment = (float) ConfigDynamicDifficulty.weightPlayerEquipment;
		float weightPlayerServerTime = (float) ConfigDynamicDifficulty.weightPlayerServerTime;
		float weightDPS = (float) ConfigDynamicDifficulty.weightDPS;
		float weightDistFromSpawn = (float) ConfigDynamicDifficulty.weightDistFromSpawn;

		//buffs that arent added into the total weight
		float weightHealth = (float) ConfigDynamicDifficulty.weightHealth;
		float weightBuffedLocation = (float) ConfigDynamicDifficulty.weightBuffedLocation;
		float weightDebuffedLocation = (float) ConfigDynamicDifficulty.weightDebuffedLocation;
		
		float weightTotal = weightPosOccupy + weightPlayerEquipment + weightPlayerServerTime + weightDPS/* + weightHealth*/ + weightDistFromSpawn/* + weightBuffedLocation*/;
		
		float difficultyPosOccupy = getDifficultyScaleForPosOccupyTime(world, pos) * weightPosOccupy;
		float difficultyPlayerEquipment = getDifficultyScaleForPlayerEquipment(player) * weightPlayerEquipment;
		float difficultyPlayerServerTime = getDifficultyScaleForPlayerServerTime(player) * weightPlayerServerTime;
		float difficultyDPS = getDifficultyScaleForPosDPS(world, pos) * weightDPS;
		float difficultyHealth = getDifficultyScaleForHealth(player) * weightHealth;
		float difficultyDistFromSpawn = getDifficultyScaleForDistFromSpawn(player) * weightDistFromSpawn;
		float difficultyBuffedLocation = getDifficultyForBuffedLocation(world, pos) * weightBuffedLocation;
		float difficultyDebuffedLocation = getDifficultyForDebuffedLocation(world, pos) * weightDebuffedLocation;

		float difficultyBuffInvasionSkip = getInvasionSkipBuff(player);
		
		float difficultyTotal = difficultyPosOccupy + difficultyPlayerEquipment + difficultyPlayerServerTime + difficultyDPS + difficultyHealth + difficultyDistFromSpawn + difficultyBuffedLocation;

		//debuff
		difficultyTotal += difficultyDebuffedLocation;

		difficultyTotal += difficultyBuffInvasionSkip;

		float val = difficultyTotal / weightTotal;//(difficultyPos + difficultyPlayerEquipment + difficultyPlayerServerTime) / 3F;
		val = Math.round(val * 1000F) / 1000F;
		if (ConfigDynamicDifficulty.difficulty_Max != -1) {
			if (val > ConfigDynamicDifficulty.difficulty_Max) {
				val = (float) ConfigDynamicDifficulty.difficulty_Max;
			}
		}

		//account for debuffs potentially causing less than zero value
		if (val < 0) {
			val = 0;
		}

		return val;
	}
	
	public static float getDifficultyScaleForPlayerServerTime(PlayerEntity player) {
		long maxServerTime = ConfigDynamicDifficulty.difficulty_MaxTicksOnServer;
		long curServerTime = player.getPersistentData().getLong(dataPlayerServerTicks);
		return Math.round(MathHelper.clamp((float)curServerTime / (float)maxServerTime, 0F, 1F) * 1000F) / 1000F;
	}
	
	public static float getDifficultyScaleForPlayerEquipment(PlayerEntity player) {
		boolean calcWeapon = false;
		int curRating = 0;
		if (player.getPersistentData().contains(dataPlayerLastCacheEquipmentRating)) {
			if (player.world.getGameTime() % 200 == 0) {
				curRating = UtilPlayer.getPlayerRating(player, calcWeapon);
				player.getPersistentData().putInt(dataPlayerLastCacheEquipmentRating, curRating);
			} else {
				curRating = player.getPersistentData().getInt(dataPlayerLastCacheEquipmentRating);
			}
		} else {
			curRating = UtilPlayer.getPlayerRating(player, calcWeapon);
			player.getPersistentData().putInt(dataPlayerLastCacheEquipmentRating, curRating);
		}
		
		int bestRating = getBestPlayerRatingPossibleVanilla(calcWeapon);
		
		//allow a scale value over 1F, means theres equipment in play beyond vanilla stuff, or i miscalculated some things
		return (float)curRating / (float)bestRating;
	}
	
	public static float getDifficultyScaleForHealth(PlayerEntity player) {
		float baseMax = ConfigDynamicDifficulty.difficulty_BestVanillaHealth;
		float curMax = player.getMaxHealth();
		float scale = curMax / baseMax;
		return scale - 1F;
	}
	
	public static float getDifficultyScaleForDistFromSpawn(PlayerEntity player) {
		
		float distX = (float) (player.world.getSpawnPoint().getX() - player.posX);
		float distZ = (float) (player.world.getSpawnPoint().getZ() - player.posZ);
		
		float dist = (float) Math.sqrt(distX * distX + distZ * distZ);
		
		dist = Math.min(dist, ConfigDynamicDifficulty.difficulty_DistFromSpawnMax);
		
		return (float)dist / (float)ConfigDynamicDifficulty.difficulty_DistFromSpawnMax;
		
	}
	
	public static int getBestPlayerRatingPossibleVanilla(boolean calcWeapon) {
		//diamond armor
		int bestArmor = ConfigDynamicDifficulty.difficulty_BestVanillaArmor;
		//protection 5 on diamond armor (there is randomization)
		int bestArmorEnchant = ConfigDynamicDifficulty.difficulty_BestVanillaArmorEnchant;
		int bestWeapon = 8;
		//6.25 for sharpness 5
		int bestWeaponEnchant = 6;
		
		if (!calcWeapon) {
			bestWeapon = 0;
			bestWeaponEnchant = 0;
		}
		
		//best for vanilla stuff is about 60?
		int bestVal = bestArmor + bestArmorEnchant + bestWeapon + bestWeaponEnchant;
		return bestVal;
	}
	
	public static float getBestPlayerDPSRatingPossibleVanilla() {
		//just a guess based on me going around with a plain diamond sword, guessing with enchanted extra
		return (float)ConfigDynamicDifficulty.difficulty_BestVanillaDPS;
	}

	public static float getDifficultyScaleForPosDPS(World world, BlockCoord pos) {
		return getDifficultyScaleForPosDPS(world, pos, false, null);
	}
	
	public static float getDifficultyScaleForPosDPS(World world, BlockCoord pos, boolean extraDebug, PlayerEntity playerToMsg) {
		int chunkRange = ConfigDynamicDifficulty.difficulty_BestDPSChunkRadius;
		int chunkX = pos.getX() / 16;
		int chunkZ = pos.getZ() / 16;
		//int count = 0;
		float bestDPS = 0;
		DamageSourceEntry bestSource = null;
		for (int x = chunkX - chunkRange; x <= chunkX + chunkRange; x++) {
			for (int z = chunkZ - chunkRange; z <= chunkZ + chunkRange; z++) {
				BlockCoord checkPos = new BlockCoord(x * 16 + 8, 128, z * 16 + 8);
				if (world.isBlockLoaded(checkPos.toBlockPos())) {
					Chunk chunk = world.getChunkAt(new BlockPos(checkPos.posX, checkPos.posY, checkPos.posZ));
					if (chunk != null) {
						ChunkDataPoint cdp = WorldDirectorManager.instance().getChunkDataGrid(world).getChunkData(x, z);

						if (extraDebug) {
							dbgHighDamage("dps debug output for " + x + ", " + z + ": " + cdp.highestDamage.toString());
						}

						if (cdp.averageDPS > bestDPS) {
							bestDPS = cdp.averageDPS;
							bestSource = cdp.highestDamage;
						}
					}
				}
			}
		}

		if (bestSource != null && playerToMsg != null) {
			playerToMsg.sendMessage(new StringTextComponent("Best average dps found:"));
			playerToMsg.sendMessage(new StringTextComponent(bestSource.toString()));
		}
		
		float scale = convertDPSToDifficultyScale(bestDPS);
		return scale;
	}

	public static void setDifficultyScaleForPosDPS(World world, BlockCoord pos, float dps, int chunkRange) {
		int chunkX = pos.getX() / 16;
		int chunkZ = pos.getZ() / 16;
		//int count = 0;
		//float bestDPS = 0;
		for (int x = chunkX - chunkRange; x < chunkX + chunkRange; x++) {
			for (int z = chunkZ - chunkRange; z < chunkZ + chunkRange; z++) {
				BlockCoord checkPos = new BlockCoord(x * 16 + 8, 128, z * 16 + 8);
				if (world.isBlockLoaded(checkPos.toBlockPos())) {
					Chunk chunk = world.getChunkAt(new BlockPos(checkPos.posX, checkPos.posY, checkPos.posZ));
					if (chunk != null) {
						ChunkDataPoint cdp = WorldDirectorManager.instance().getChunkDataGrid(world).getChunkData(x, z);

						cdp.averageDPS = dps;
						cdp.lastDPSRecalc = 0;
						cdp.listDPSAveragesShortTerm.clear();
						cdp.listDPSAveragesLongTerm.clear();
						cdp.highestDamage = new DamageSourceEntry();
						cdp.highestDamage.source_type = "<COMMAND SET>";

						/*if (cdp.averageDPS > bestDPS) {
							bestDPS = cdp.averageDPS;
						}*/
					}
				}
			}
		}
		//long averageTime = bestTime / count;

		//float scale = convertDPSToDifficultyScale(bestDPS);
	}
	
	public static float convertDPSToDifficultyScale(float dps) {
		float scale = dps / getBestPlayerDPSRatingPossibleVanilla();
		if (scale > ConfigCoroUtilAdvanced.difficulty_MaxDPSRatingAllowed) {
			scale = (float) ConfigCoroUtilAdvanced.difficulty_MaxDPSRatingAllowed;
		}
		return scale;
	}
	
	public static float getDifficultyScaleForPosOccupyTime(World world, BlockCoord pos) {
		/**
		 * 1 chunk calc
		 */
		/*Chunk chunk = world.getChunkAt(pos);
		if (chunk != null) {
			long inhabTime = chunk.getInhabitedTime();
			float scale = convertInhabTimeToDifficultyScale(inhabTime);
			return scale;
			
		}
		return 0F;*/
		
		/**
		 * average radius calc
		 */
		int chunkRange = 3;
		int chunkX = pos.getX() / 16;
		int chunkZ = pos.getZ() / 16;
		int count = 0;
		long totalTime = 0;
		for (int x = chunkX - chunkRange; x < chunkX + chunkRange; x++) {
			for (int z = chunkZ - chunkRange; z < chunkZ + chunkRange; z++) {
				BlockCoord checkPos = new BlockCoord(x * 16 + 8, 128, z * 16 + 8);
				if (world.isBlockLoaded(checkPos.toBlockPos())) {
					Chunk chunk = world.getChunkAt(new BlockPos(checkPos.posX, checkPos.posY, checkPos.posZ));
					if (chunk != null) {
						totalTime += chunk.getInhabitedTime();
						count++;
					}
				}
			}
		}
		long averageTime = 0;
		if (count > 0) {
			averageTime = totalTime / count;
		}
		
		float scale = convertInhabTimeToDifficultyScale(averageTime);
		return Math.round(scale * 1000F) / 1000F;
		
		/**
		 * best chunk count
		 */
		/*int chunkRange = 4;
		int chunkX = pos.getX() / 16;
		int chunkZ = pos.getZ() / 16;
		//int count = 0;
		long bestTime = 0;
		for (int x = chunkX - chunkRange; x < chunkX + chunkRange; x++) {
			for (int z = chunkZ - chunkRange; z < chunkZ + chunkRange; z++) {
				BlockCoord checkPos = new BlockCoord(x * 16 + 8, 128, z * 16 + 8);
				if (world.isBlockLoaded(checkPos)) {
					Chunk chunk = world.getChunkAt(checkPos);
					if (chunk != null) {
						if (chunk.getInhabitedTime() > bestTime) {
							bestTime = chunk.getInhabitedTime();
						}
					}
				}
			}
		}
		//long averageTime = bestTime / count;
		
		float scale = convertInhabTimeToDifficultyScale(bestTime);
		return scale;*/
	}

	public static float getInvasionSkipBuff(PlayerEntity player) {
		return player.getPersistentData().getFloat(dataPlayerInvasionSkipBuff);
		/*float buffBase = 0.5F;
		float skipCount = player.getPersistentData().getInt(dataPlayerInvasionSkipCount);
		float val = buffBase * skipCount;
		return val;*/
	}

	public static void setInvasionSkipBuff(PlayerEntity player, float buff) {
		player.getPersistentData().putFloat(dataPlayerInvasionSkipBuff, buff);
	}
	
	/**
	 * 
	 * Returns value between 0 and 1 based on configured values
	 * 
	 * @param inhabTime
	 * @return
	 */
	public static float convertInhabTimeToDifficultyScale(long inhabTime) {
		float scale = (float)inhabTime / (float)ConfigDynamicDifficulty.difficulty_MaxTicksInChunk;
		return scale;
	}
	
	public static void handleHarvest(HarvestDropsEvent event) {
		if (event.getHarvester() != null) {
			if (event.getWorld().playerEntities.contains(event.getHarvester())) {
				
				CompoundNBT nbt = event.getHarvester().getPersistentData();//WorldDirectorMultiDim.getPlayerNBT(CoroUtilEntity.getName(event.harvester));
				if (event.getState() != null && event.getState().getBlock() instanceof OreBlock) {
					int curVal = nbt.getInt(dataPlayerHarvestOre);
					curVal++;
					nbt.putInt(dataPlayerHarvestOre, curVal);
					//System.out.println("increment!");
				} else if (event.getState() != null && event.getState().getBlock() instanceof LogBlock) {
					int curVal = nbt.getInt(dataPlayerHarvestLog);
					curVal++;
					nbt.putInt(dataPlayerHarvestLog, curVal);
				}
				
				/*float curVal = nbt.getFloat(dataPlayerHarvestRating);
				curVal += getBlockImportanceValue(event.block);
				nbt.putFloat(dataPlayerHarvestRating, curVal);*/
				increaseInvadeRating(event.getHarvester(), getBlockImportanceValue(event.getState().getBlock()));
				
				//System.out.println("harvested block for " + event.harvester.username + " - " + event.block);
			}
		}
	}
	
	public static void increaseInvadeRating(PlayerEntity parPlayer, float parVal) {
		CompoundNBT nbt = parPlayer.getPersistentData();//WorldDirectorMultiDim.getPlayerNBT(CoroUtilEntity.getName(parPlayer));
		float curVal = nbt.getFloat(dataPlayerHarvestRating);
		curVal += parVal;
		nbt.putFloat(dataPlayerHarvestRating, curVal);
		
		//System.out.println("curVal: " + curVal);
	}
	
	public static void decreaseInvadeRating(PlayerEntity parPlayer, float parVal) {
		CompoundNBT nbt = parPlayer.getPersistentData();//WorldDirectorMultiDim.getPlayerNBT(CoroUtilEntity.getName(parPlayer));
		float curVal = nbt.getFloat(dataPlayerHarvestRating);
		curVal -= parVal;
		nbt.putFloat(dataPlayerHarvestRating, curVal);
	}
	
	public static float getHarvestRatingInvadeThreshold() {
		return 30F;
	}
	
	public static boolean isInvadeable(PlayerEntity parPlayer) {
		return parPlayer.getPersistentData().getFloat(dataPlayerHarvestRating) >= getHarvestRatingInvadeThreshold();
	}
	
	public static float getBlockImportanceValue(Block block) {

		try {
			boolean test = false;
			if (test) {
				System.out.println("TEST INVADE IS ON!");
				return 30;
			}

			float scaleBase = 1F;
			float defaultIron = scaleBase * 0.3F;

			if (block instanceof LogBlock) {
				return scaleBase * 0.1F;
			} else if (block instanceof SaplingBlock) {
				return scaleBase * 0.3F;
			} else if (block instanceof OreBlock) {
				if (block == Blocks.COAL_ORE) {
					return scaleBase * 0.2F;
				} else if (block == Blocks.IRON_ORE) {
					return defaultIron;
				} else if (block == Blocks.GOLD_ORE) {
					return scaleBase * 0.4F;
				} else if (block == Blocks.LIT_REDSTONE_ORE || block == Blocks.REDSTONE_ORE) {
					return scaleBase * 0.5F;
				} else if (block == Blocks.LAPIS_ORE) {
					return scaleBase * 0.6F;
				} else if (block == Blocks.DIAMOND_ORE) {
					return scaleBase * 1F;
				} else if (block == Blocks.EMERALD_ORE) {
					return scaleBase * 1.2F;
				} else {
					return defaultIron;
				}
				//TODO: possibly risky use of oredict, see issue #267, NPE at CoroUtil.difficulty.DynamicDifficulty.getBlockImportanceValue(DynamicDifficulty.java:569)
			} else if (OreDictionary.getOres(Block.REGISTRY.getKey(block).toString()).size() > 0) {
				return defaultIron;
			} else {
				return 0;
			}
		} catch (Exception ex) {
			//workaround for issue #267
		}
		return 0;
	}
	
	public static PlayerEntity getBestPlayerForArea(World world, BlockCoord pos) {
		
		PlayerEntity player = world.getClosestPlayer(pos.posX, pos.posY, pos.posZ, -1, false);
		
		return player;
	}
	
	public static void logDamage(LivingHurtEvent event) {

		boolean tempLog = ConfigCoroUtilAdvanced.logging_DPS_All;
		if (tempLog) {
			try {
				System.out.println("LivingHurtEvent: ");
				System.out.println("type: " + event.getSource().damageType);
				if (event.getSource().getImmediateSource() != null)
					System.out.println("source immediate: " + event.getSource().getImmediateSource().getName());
				if (event.getSource().getTrueSource() != null)
					System.out.println("source true: " + event.getSource().getTrueSource().getName());
				System.out.println("amount: " + event.getAmount());


				Throwable t = new Throwable();
				t.printStackTrace();

			} catch (Exception ex) {
				//no
			}
		}

		if (event.getEntity().world.isRemote) return;
		if (ConfigCoroUtilAdvanced.trackChunkData) {
			
			Entity ent = event.getEntity();
			World world = ent.world;
			
			if (ent instanceof CreatureEntity && (!ConfigDynamicDifficulty.difficulty_OnlyLogDPSToHostiles || ent instanceof IMob)) {
				CreatureEntity entC = (CreatureEntity) ent;

				//dont log common occuring damages, sun burning, random wall glitching
				if (ConfigDynamicDifficulty.difficulty_DontLogDPSFromEnvironment) {
					if (event.getSource() == DamageSource.IN_WALL ||
							event.getSource() == DamageSource.IN_FIRE ||
							event.getSource() == DamageSource.ON_FIRE ||
							event.getSource() == DamageSource.DROWN ||
							event.getSource() == DamageSource.FALL) {
						dbgDPS("bad damage type, skipping: " + event.getSource());
						return;
					}
				}

				//must always exclude, /kill command uses it
				if (event.getSource() == DamageSource.OUT_OF_WORLD) {
					return;
				}

				if (ConfigDynamicDifficulty.difficulty_OnlyLogDPSFromPlayerAsSource) {
					if (!(event.getSource().getImmediateSource() instanceof PlayerEntity) && !(event.getSource().getTrueSource() instanceof PlayerEntity)) {
						return;
					}
				}

				//dont log sources from AI, this fixes things like creepers damaging zombies, wolfs attacking sheep, etc
				//unless its a pet of a player
				if (event.getSource().getTrueSource() instanceof MobEntity) {
					if (event.getSource().getTrueSource() instanceof IEntityOwnable) {
						if (((IEntityOwnable) event.getSource().getTrueSource()).getOwner() instanceof PlayerEntity) {
							//this is fine
						} else {
							return;
						}
					} else {
						return;
					}
                }

				//dont process bad sources, like mob grinders
				if (listBlacklistedDamageSources.size() > 0) {
					if (stringEqualsItemFromList(event.getSource().damageType, listBlacklistedDamageSources)) {
						dbgDPS("Detected blacklisted damage type, skipping: " + event.getSource().damageType);
						return;
					}

					if (event.getSource().getTrueSource() != null && stringEqualsItemFromList(event.getSource().getTrueSource().getName(), listBlacklistedDamageSources)) {
						dbgDPS("Detected blacklisted entity name for true source, skipping: " + event.getSource().getTrueSource().getName());
						return;
					}

					if (event.getSource().getImmediateSource() != null && stringEqualsItemFromList(event.getSource().getImmediateSource().getName(), listBlacklistedDamageSources)) {
						dbgDPS("Detected blacklisted entity name immediate source, skipping: " + event.getSource().getImmediateSource().getName());
						return;
					}
				}
				
				
				AttackData log;
				if (!lookupEntToDamageLog.containsKey(ent.getEntityId())) {
					log = new AttackData(entC);
					lookupEntToDamageLog.put(ent.getEntityId(), log);
				} else {
					int lastLogTimeThreshold = 20*5;
					log = lookupEntToDamageLog.get(ent.getEntityId());

					//if it took too long for next hit, do a full reset
					//lets also not log the damage, as it'd be logged as a sloooow attack, lets hope for better data to come
					if (log.getLastLogTime() + lastLogTimeThreshold < world.getGameTime()) {
						//dbgDPS("damage expired, resetting");
						logToChunk(log);
						log.cleanup();
						log = new AttackData(entC);
						lookupEntToDamageLog.put(ent.getEntityId(), log);
					} else if (!log.isSameSource(event.getSource())) {
						dbgDPS("damage source mismatch, resetting");
						//if a different source is damaging it, do a full reset
						//TODO: it is possible that no dps will ever get logged if a perfect cycle of 2 alternating damage sources are hitting it
						//a solution would be to have multiple damage source trackers per entity
						//might be overkill, we dont have to track everything perfectly
						logToChunk(log);
						log.cleanup();
						log = new AttackData(entC);
						lookupEntToDamageLog.put(ent.getEntityId(), log);
					}
				}

				//we do nothing with first freshly tracked hit
				//we do nothing with last timed out hit
				//only if same source of hit happens again within 5 seconds, track it
				//- we actually track single hits with no follow up, will count as instahit of half second interval
				//prevent less than 10 tick hits to protect against potential bug
				//if same tick damage happens from same source, assume a mod is just doing odd things and add them up to be 1 hit


				//TODO: TRACK SOURCE OF DAMAGE AND RESET IF IT DIFFERS! THIS IS THE BUG WITH DPS
				//hitting right after fire damage = high dps
				//if source mismatch, log last damage and reset
				//or just reset?

				/**
				 *
				 * shortest time allowed: 10 ticks
				 *
				 * longest time allowed: 20*5 ticks
				 * - if this is hit, dont count it, reset
				 *
				 * scenarios:
				 *
				 * normal:
				 *
				 * log damage source 1
				 * small time passes
				 * log damage source 1
				 * - calculates dps
				 * - cycle can repeat
				 *
				 *
				 */

				float damageToLog = event.getAmount();
				float timeDiffSeconds = 0;
				float damage = 0;
				boolean bigDamage = false;
				if (log.getLastDamage() > 0) {
					long timeDiff = world.getGameTime() - log.getLastLogTime();

					//catch potentially game breaking fast hits, mainly to fix buggy edge cases, might not be needed with new source tracking code
					if (timeDiff != 0 && ConfigCoroUtilAdvanced.difficulty_MaxAttackSpeedLoggable != -1 && timeDiff < ConfigCoroUtilAdvanced.difficulty_MaxAttackSpeedLoggable) {
						dbgDPS("DPS WARNING: detected high hit rate of " + timeDiff + ", adjusting to max allowed rate of " + ConfigCoroUtilAdvanced.difficulty_MaxAttackSpeedLoggable);
						timeDiff = (long) ConfigCoroUtilAdvanced.difficulty_MaxAttackSpeedLoggable;
					}

					timeDiffSeconds = (float)timeDiff / 20F;
					if (timeDiff > 0) {
						damage = log.getLastDamage() / timeDiffSeconds;


						
						if (ConfigCoroUtilAdvanced.difficulty_MaxDPSLoggable != -1 && damage > ConfigCoroUtilAdvanced.difficulty_MaxDPSLoggable) {
							damage = (float) ConfigCoroUtilAdvanced.difficulty_MaxDPSLoggable;
							dbgDPS("DPS WARNING: !!!!!!!!!!!!!! we hit a max loggable damage scenario!!!!!!!!!");
							bigDamage = true;
						}

						if (damage > ConfigDynamicDifficulty.difficulty_BestVanillaDPS) {
							dbgDPS("DPS WARNING: !!! damage was greater than expected vanilla abilities!!!");
							bigDamage = true;
						}

						//log.setTimeAveragedDamage(damage);
						log.getListDPSs().add(damage);
						
						//System.out.println("dps log: " + damage + " new Damage: " + event.ammount + " tickDiff: " + timeDiff + " source: " + event.source.damageType + " ID: " + ent.getEntityId());
						
						
					} else {
						//if no time passed, just add last entry onto current entry
						damageToLog += log.getLastDamage();
					}
				}

				if (bigDamage) {
					String source = "null";
					if (event.getSource().getTrueSource() != null) {
						source = event.getSource().getTrueSource().getClass().getSimpleName();
					}
					dbgDPS("logging damageToLog: " + damageToLog + " damage: " + damage + ", log.getLastDamage(): " + log.getLastDamage() + " to " + entC.getClass().getSimpleName() + " by source " + source + " with " + event.getSource().damageType + " timediffinsecs: " + timeDiffSeconds);
				}

				log.trackSources(event.getSource());
				log.setLastDamage(damageToLog);
				log.setLastLogTime(world.getGameTime());
				//log.setSource_pos(ent.getPosition());

				//keep track of highest damage out to maybe log to chunk
				if (damageToLog > log.highestDamage.highestDamage) {
					log.highestDamage.highestDamage = damageToLog;
					log.highestDamage.damageTimeAveraged = damage;
					log.highestDamage.source_type = log.getSource_type();
					log.highestDamage.source_entity_true = log.getSource_entityTrue() != null ?
							log.getSource_entityTrue().getClass().getSimpleName() + ", entity_name: " + log.getSource_entityTrue().getName() : "<NULL>";
					log.highestDamage.source_entity_immediate = log.getSource_entityImmediate() != null ?
							log.getSource_entityImmediate().getClass().getSimpleName() + ", entity_name: " + log.getSource_entityImmediate().getName() : "<NULL>";
					log.highestDamage.target_entity = ent != null ? ent.getClass().getSimpleName() + ", entity_name: " + ent.getName() : "<NULL>";
					log.highestDamage.lastLogTime = log.getLastLogTime();
					log.highestDamage.timeDiffSeconds = timeDiffSeconds;
					log.highestDamage.source_pos = ent.getPosition();
				}
			}
			
			
		}
		
	}
	
	public static void logDeath(LivingDeathEvent event) {
		if (event.getEntity().world.isRemote) return;
		if (ConfigCoroUtilAdvanced.trackChunkData) {
			
			Entity ent = event.getEntity();
			
			if (ent instanceof CreatureEntity && (!ConfigDynamicDifficulty.difficulty_OnlyLogDPSToHostiles || ent instanceof IMob)) {
				if (lookupEntToDamageLog.containsKey(ent.getEntityId())) {
					AttackData log = lookupEntToDamageLog.get(ent.getEntityId());
					if (log != null) {
						logToChunk(log);
						log.cleanup();
						lookupEntToDamageLog.remove(ent.getEntityId());
					}
				}
				
			}
			
		}
	}
	
	public static void logToChunk(AttackData log) {
		int maxShortTermSize = 50;
		int maxLongTermSize = 50;
		int recalcRate = 20*2;
		
		CreatureEntity ent = log.getEnt();
		World world = ent.world;
		int chunkX = MathHelper.floor(ent.posX / 16);
		int chunkZ = MathHelper.floor(ent.posZ / 16);
		ChunkDataPoint cdp = WorldDirectorManager.instance().getChunkDataGrid(world).getChunkData(chunkX, chunkZ);
		
		if (log.getListDPSs().size() == 0 && log.getLastDamage() > 0) {
			//add an insta kill dps that assumes can be done every half second
			float instaKillDPSCalc = log.getLastDamage() * 2;
			
			if (ConfigCoroUtilAdvanced.difficulty_MaxDPSLoggable != -1 && instaKillDPSCalc > ConfigCoroUtilAdvanced.difficulty_MaxDPSLoggable) {
				instaKillDPSCalc = (float) ConfigCoroUtilAdvanced.difficulty_MaxDPSLoggable;
			}

			dbgDPS("logging one time hit of: " + instaKillDPSCalc);
			log.getListDPSs().add(instaKillDPSCalc);
		}
		
		if (log.getListDPSs().size() > 0) {
			float avgDPS = 0;
			for (float val : log.getListDPSs()) {
				avgDPS += val;
			}
			avgDPS /= log.getListDPSs().size();

			dbgDPS("logging short term average: " + avgDPS);
			cdp.listDPSAveragesShortTerm.add(avgDPS);

			if (log.getLastDamage() > cdp.highestDamage.highestDamage) {
				cdp.highestDamage = log.highestDamage;
				dbgHighDamage("New highest damage for chunk " + chunkX + ", " + chunkZ + ": " + log.highestDamage.toString());
			}
		}
		
		//trim list
		if (cdp.listDPSAveragesShortTerm.size() > maxShortTermSize) {
			cdp.listDPSAveragesShortTerm.remove(0);
		}
		
		//if time to do a full recalc
		if (cdp.lastDPSRecalc + recalcRate < world.getGameTime()) {
			
			if (cdp.listDPSAveragesShortTerm.size() > 0) {
				float avgDPS2 = 0;
				for (float val : cdp.listDPSAveragesShortTerm) {
					avgDPS2 += val;
				}
				avgDPS2 /= cdp.listDPSAveragesShortTerm.size();

				dbgDPS("logging long term average: " + avgDPS2);
				cdp.listDPSAveragesLongTerm.add(avgDPS2);
			}
			if (cdp.listDPSAveragesLongTerm.size() > maxLongTermSize) {
				cdp.listDPSAveragesLongTerm.remove(0);
			}
			
			if (cdp.listDPSAveragesLongTerm.size() > 0) {
				float avgDPS3 = 0;
				for (float val : cdp.listDPSAveragesLongTerm) {
					avgDPS3 += val;
				}
				avgDPS3 /= cdp.listDPSAveragesLongTerm.size();

				dbgDPS("logging chunk dps average: " + avgDPS3);
				cdp.averageDPS = avgDPS3;
				
				//System.out.println("average of the average of the average: " + avgDPS3);
			}
			
			cdp.lastDPSRecalc = world.getGameTime();
		}
	}



	public static BuffedLocation buffLocation(World world, BlockCoord coord, int distRadius, float difficulty) {
		BuffedLocation zone = new BuffedLocation(distRadius, difficulty);
		zone.setWorldID(world.provider.getDimension());
		zone.setOrigin(coord);
		WorldDirectorManager.instance().getCoroUtilWorldDirector(world).addTickingLocation(zone);
		return zone;
	}

	public static float getDifficultyForDebuffedLocation(World world, BlockCoord coord) {
		return getDifficultyForBuffedLocation(world, coord, true);
	}

	public static float getDifficultyForBuffedLocation(World world, BlockCoord coord) {
		return getDifficultyForBuffedLocation(world, coord, false);
	}

	public static float getDifficultyForBuffedLocation(World world, BlockCoord coord, boolean findDebuffInstead) {
		//TODO: cache results to minimize lookup thrash
		float bestDifficulty = 0;
		//BuffedLocation bestBuff = null;
		WorldDirector wd = WorldDirectorManager.instance().getCoroUtilWorldDirector(world);
		for (ISimulationTickable loc : wd.listTickingLocations) {
			if (loc instanceof BuffedLocation) {
				BuffedLocation buff = (BuffedLocation)loc;
				if (buff.isDebuff() == findDebuffInstead) {
					double dist = coord.getDistanceSquared(buff.getOrigin());
					if (dist < buff.buffDistRadius * buff.buffDistRadius) {
						if (findDebuffInstead) {
							if (buff.difficulty < bestDifficulty) {
								bestDifficulty = buff.difficulty;
							}
						} else {
							if (buff.difficulty > bestDifficulty) {
								bestDifficulty = buff.difficulty;
							}
						}
					}
				}
			}
		}

		return bestDifficulty;
	}

	public static boolean stringEqualsItemFromList(String inputStr, List<String> items) {
		return items.parallelStream().anyMatch(inputStr::equals);
	}



	public static void dbgDPS(String string) {
		if (ConfigCoroUtilAdvanced.logging_DPS_Fine) {
			CULog.log(string);
		}
	}

	public static void dbgHighDamage(String string) {
		if (ConfigCoroUtilAdvanced.logging_DPS_HighSources) {
			CULog.log(string);
		}
	}
	
	
	
}
