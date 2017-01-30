package CoroUtil.difficulty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import CoroUtil.world.WorldDirector;
import CoroUtil.world.location.ISimulationTickable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.oredict.OreDictionary;
import CoroUtil.config.ConfigCoroAI;
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

	public static String dataPlayerInvasionSkipBuff = "HW_dataPlayerInvasionSkipBuff";
	
	private static int tickRate = 20;
	
	public static HashMap<Integer, AttackData> lookupEntToDamageLog = new HashMap<Integer, AttackData>();

	public static void tickServer(ServerTickEvent event) {
		World world = DimensionManager.getWorld(0);
		if (world != null) {
			for (Object player : world.playerEntities) {
				if (player instanceof EntityPlayer) {
					tickPlayer((EntityPlayer)player);
				}
			}
			
			
			if (ConfigCoroAI.cleanupStrayMobs) {
				long dayNumber = (world.getWorldTime() / 24000) + 1;
				if (dayNumber % ConfigCoroAI.cleanupStrayMobsDayRate == 0) {
					long timeOfDay = world.getWorldTime() % 24000;
					int killTimeRange = 10;
					if (timeOfDay >= (long)ConfigCoroAI.cleanupStrayMobsTimeOfDay && timeOfDay < (long)(2000+killTimeRange)) {
						System.out.println("KILLING ALL ZOMBIES!");
						for (Object obj : world.getLoadedEntityList()) {
							if (obj instanceof EntityZombie) {
								((EntityZombie) obj).setDead();
							}
						}
					}
				}
			}
		}
	}
	
	public static void tickPlayer(EntityPlayer player) {
		World world = player.worldObj;
		if (world.getTotalWorldTime() % tickRate == 0) {
			
			long ticksPlayed = player.getEntityData().getLong(dataPlayerServerTicks);
			ticksPlayed += 20;
			//3 hour start debug
			//ticksPlayed = 20*60*60*3;
			player.getEntityData().setLong(dataPlayerServerTicks, ticksPlayed);
			
		}

		int wallScanRange = 3;

		boolean autoAttackTest = true;
		boolean isInAir = false;
		
		boolean dbg = false;
		
		if (dbg) System.out.println("player tick");
		
		if ((!player.capabilities.isCreativeMode || autoAttackTest)) {
			if (dbg) System.out.println("1");
    		if ((player.capabilities.isFlying || (!player.onGround && !player.isInWater() && !player.isInsideOfMaterial(Material.LAVA)))) {
    			if (dbg) System.out.println("2");
    			if (player.getRidingEntity() == null) {
    				if (dbg) System.out.println("3");
    				Block block = null;
    				int pX = MathHelper.floor_double(player.posX);
    				int pY = MathHelper.floor_double(player.getEntityBoundingBox().minY);
    				int pZ = MathHelper.floor_double(player.posZ);
    				boolean foundWall = false;
    				for (int x = -wallScanRange; !foundWall && x <= wallScanRange; x++) {
    					for (int z = -wallScanRange; !foundWall && z <= wallScanRange; z++) {
    						for (int y = -wallScanRange; !foundWall && y <= wallScanRange; y++) {
								BlockPos pos = new BlockPos(pX+x, pY+y, pZ+z);
    							IBlockState state = world.getBlockState(pos);
    							block = state.getBlock();
    							if (!block.isAir(state, world, pos)) {
    								List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
    								block.addCollisionBoxToList(state, world, new BlockPos(pX+x, pY+y, pZ+z), player.getEntityBoundingBox(), list, player);
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
			long airTime = player.getEntityData().getLong(dataPlayerDetectInAirTime);
			player.getEntityData().setLong(dataPlayerDetectInAirTime, airTime+1);
		} else {
			if (dbg) System.out.println("not in air");
			player.getEntityData().setLong(dataPlayerDetectInAirTime, 0);
		}
		
	}

	public static void deathPlayer(EntityPlayer player) {
		//TODO: find existing ones and reset their timer? what about it not buffing the exact new spot

		WorldDirector wd = WorldDirectorManager.instance().getCoroUtilWorldDirector(player.worldObj);

		if (wd != null) {
			BuffedLocation debuff = new BuffedLocation(32, -2);
			debuff.setDecays(true);
			wd.addTickingLocation(debuff);
		}
	}

	public static float getDifficultyAveragedForArea(EntityCreature spawnedEntity) {
		return getDifficultyAveragedForArea(spawnedEntity, spawnedEntity.getPosition());
	}

	public static float getDifficultyAveragedForArea(EntityCreature spawnedEntity, BlockPos pos) {
		return getDifficultyAveragedForArea(spawnedEntity, pos.getX(), pos.getY(), pos.getZ());
	}

	public static float getDifficultyAveragedForArea(EntityCreature spawnedEntity, int x, int y, int z) {

		//TODO: cache so there is less player lookup thrashing for spawn candidate code
		EntityPlayer player = spawnedEntity.worldObj.getClosestPlayerToEntity(spawnedEntity, -1);

		if (player != null) {
			return getDifficultyScaleAverage(player, x, y, z);
		}

		return 0;
	}
	
	public static float getDifficultyScaleAverage(EntityPlayer player, int x, int y, int z) {
		return getDifficultyScaleAverage(player, new BlockCoord(x, y, z));
	}
	
	public static float getDifficultyScaleAverage(World world, EntityPlayer player, BlockCoord pos) {
		return getDifficultyScaleAverage(player, pos);
	}
	
	public static float getDifficultyScaleAverage(EntityPlayer player, BlockCoord pos) {
		
		World world = player.worldObj;
		
		//test
		//if (true) return 2F;
		
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
	
	public static float getDifficultyScaleForPlayerServerTime(EntityPlayer player) {
		long maxServerTime = ConfigDynamicDifficulty.difficulty_MaxTicksOnServer;
		long curServerTime = player.getEntityData().getLong(dataPlayerServerTicks);
		return Math.round(MathHelper.clamp_float((float)curServerTime / (float)maxServerTime, 0F, 1F) * 1000F) / 1000F;
	}
	
	public static float getDifficultyScaleForPlayerEquipment(EntityPlayer player) {
		boolean calcWeapon = false;
		int curRating = 0;
		if (player.getEntityData().hasKey(dataPlayerLastCacheEquipmentRating)) {
			if (player.worldObj.getTotalWorldTime() % 200 == 0) {
				curRating = UtilPlayer.getPlayerRating(player, calcWeapon);
				player.getEntityData().setInteger(dataPlayerLastCacheEquipmentRating, curRating);
			} else {
				curRating = player.getEntityData().getInteger(dataPlayerLastCacheEquipmentRating);
			}
		} else {
			curRating = UtilPlayer.getPlayerRating(player, calcWeapon);
			player.getEntityData().setInteger(dataPlayerLastCacheEquipmentRating, curRating);
		}
		
		int bestRating = getBestPlayerRatingPossibleVanilla(calcWeapon);
		
		//allow a scale value over 1F, means theres equipment in play beyond vanilla stuff, or i miscalculated some things
		return (float)curRating / (float)bestRating;
	}
	
	public static float getDifficultyScaleForHealth(EntityPlayer player) {
		float baseMax = ConfigDynamicDifficulty.difficulty_BestVanillaHealth;
		float curMax = player.getMaxHealth();
		float scale = curMax / baseMax;
		return scale - 1F;
	}
	
	public static float getDifficultyScaleForDistFromSpawn(EntityPlayer player) {
		
		float distX = (float) (player.worldObj.getSpawnPoint().getX() - player.posX);
		float distZ = (float) (player.worldObj.getSpawnPoint().getZ() - player.posZ);
		
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
		int chunkRange = ConfigDynamicDifficulty.difficulty_BestDPSRadius;
		int chunkX = pos.getX() / 16;
		int chunkZ = pos.getZ() / 16;
		//int count = 0;
		float bestDPS = 0;
		for (int x = chunkX - chunkRange; x < chunkX + chunkRange; x++) {
			for (int z = chunkZ - chunkRange; z < chunkZ + chunkRange; z++) {
				BlockCoord checkPos = new BlockCoord(x * 16 + 8, 128, z * 16 + 8);
				if (world.isBlockLoaded(checkPos.toBlockPos())) {
					Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(checkPos.posX, checkPos.posY, checkPos.posZ));
					if (chunk != null) {
						ChunkDataPoint cdp = WorldDirectorManager.instance().getChunkDataGrid(world).getChunkData(x, z);

						if (cdp.averageDPS > bestDPS) {
							bestDPS = cdp.averageDPS;
						}
					}
				}
			}
		}
		//long averageTime = bestTime / count;
		
		float scale = convertDPSToDifficultyScale(bestDPS);
		return scale;
	}
	
	public static float convertDPSToDifficultyScale(float dps) {
		float scale = (float)dps / (float)getBestPlayerDPSRatingPossibleVanilla();
		if (scale > ConfigDynamicDifficulty.difficulty_MaxDPSRatingAllowed) {
			scale = (float) ConfigDynamicDifficulty.difficulty_MaxDPSRatingAllowed;
		}
		return scale;
	}
	
	public static float getDifficultyScaleForPosOccupyTime(World world, BlockCoord pos) {
		/**
		 * 1 chunk calc
		 */
		/*Chunk chunk = world.getChunkFromBlockCoords(pos);
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
					Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(checkPos.posX, checkPos.posY, checkPos.posZ));
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
					Chunk chunk = world.getChunkFromBlockCoords(checkPos);
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

	public static float getInvasionSkipBuff(EntityPlayer player) {
		return player.getEntityData().getFloat(dataPlayerInvasionSkipBuff);
		/*float buffBase = 0.5F;
		float skipCount = player.getEntityData().getInteger(dataPlayerInvasionSkipCount);
		float val = buffBase * skipCount;
		return val;*/
	}

	public static void setInvasionSkipBuff(EntityPlayer player, float buff) {
		player.getEntityData().setFloat(dataPlayerInvasionSkipBuff, buff);
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
				
				NBTTagCompound nbt = event.getHarvester().getEntityData();//WorldDirectorMultiDim.getPlayerNBT(CoroUtilEntity.getName(event.harvester));
				if (event.getState() != null && event.getState().getBlock() instanceof BlockOre) {
					int curVal = nbt.getInteger(dataPlayerHarvestOre);
					curVal++;
					nbt.setInteger(dataPlayerHarvestOre, curVal);
					//System.out.println("increment!");
				} else if (event.getState() != null && event.getState().getBlock() instanceof BlockLog) {
					int curVal = nbt.getInteger(dataPlayerHarvestLog);
					curVal++;
					nbt.setInteger(dataPlayerHarvestLog, curVal);
				}
				
				/*float curVal = nbt.getFloat(dataPlayerHarvestRating);
				curVal += getBlockImportanceValue(event.block);
				nbt.setFloat(dataPlayerHarvestRating, curVal);*/
				increaseInvadeRating(event.getHarvester(), getBlockImportanceValue(event.getState().getBlock()));
				
				//System.out.println("harvested block for " + event.harvester.username + " - " + event.block);
			}
		}
	}
	
	public static void increaseInvadeRating(EntityPlayer parPlayer, float parVal) {
		NBTTagCompound nbt = parPlayer.getEntityData();//WorldDirectorMultiDim.getPlayerNBT(CoroUtilEntity.getName(parPlayer));
		float curVal = nbt.getFloat(dataPlayerHarvestRating);
		curVal += parVal;
		nbt.setFloat(dataPlayerHarvestRating, curVal);
		
		//System.out.println("curVal: " + curVal);
	}
	
	public static void decreaseInvadeRating(EntityPlayer parPlayer, float parVal) {
		NBTTagCompound nbt = parPlayer.getEntityData();//WorldDirectorMultiDim.getPlayerNBT(CoroUtilEntity.getName(parPlayer));
		float curVal = nbt.getFloat(dataPlayerHarvestRating);
		curVal -= parVal;
		nbt.setFloat(dataPlayerHarvestRating, curVal);
	}
	
	public static float getHarvestRatingInvadeThreshold() {
		return 30F;
	}
	
	public static boolean isInvadeable(EntityPlayer parPlayer) {
		return parPlayer.getEntityData().getFloat(dataPlayerHarvestRating) >= getHarvestRatingInvadeThreshold();
	}
	
	public static float getBlockImportanceValue(Block block) {
		
		boolean test = false;
		if (test) {
			System.out.println("TEST INVADE IS ON!");
			return 30;
		}
		
		float scaleBase = 1F;
		float defaultIron = scaleBase * 0.3F;
		
		if (block instanceof BlockLog) {
			return scaleBase * 0.1F;
		} else if (block instanceof BlockSapling) {
			return scaleBase * 0.3F;
		} else if (block instanceof BlockOre) {
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
		} else if (OreDictionary.getOres(Block.REGISTRY.getNameForObject(block).toString()).size() > 0) {
			return defaultIron;
		} else {
			return 0;
		}
	}
	
	public static EntityPlayer getBestPlayerForArea(World world, BlockCoord pos) {
		
		EntityPlayer player = world.getClosestPlayer(pos.posX, pos.posY, pos.posZ, -1, false);
		
		return player;
	}
	
	public static void logDamage(LivingHurtEvent event) {
		if (event.getEntity().worldObj.isRemote) return;
		if (ConfigDynamicDifficulty.trackChunkData) {
			
			Entity ent = event.getEntity();
			World world = ent.worldObj;
			
			if (ent instanceof IMob && ent instanceof EntityCreature) {
				EntityCreature entC = (EntityCreature) ent;
				
				//dont log common occuring damages, sun burning, random wall glitching
				if (event.getSource() == DamageSource.inWall || 
						event.getSource() == DamageSource.inFire || 
						event.getSource() == DamageSource.onFire || 
						event.getSource() == DamageSource.drown ||
						event.getSource() == DamageSource.fall/* || event.source == DamageSource.lava*/) {
					return;
				}
				
				
				
				AttackData log = null;
				if (!lookupEntToDamageLog.containsKey(ent.getEntityId())) {
					log = new AttackData(entC);
					lookupEntToDamageLog.put(ent.getEntityId(), log);
				} else {
					int lastLogTimeThreshold = 20*5;
					log = lookupEntToDamageLog.get(ent.getEntityId());
					if (log.getLastLogTime() + lastLogTimeThreshold < world.getTotalWorldTime()) {
						logToChunk(log);
						log.cleanup();
						log = new AttackData(entC);
						lookupEntToDamageLog.put(ent.getEntityId(), log);
					} else {
						
					}
				}
				
				float damageToLog = event.getAmount();
				if (log.getLastDamage() > 0) {
					long timeDiff = world.getTotalWorldTime() - log.getLastLogTime();
					float timeDiffSeconds = (float)timeDiff / 20F;
					if (timeDiff > 0) {
						float damage = log.getLastDamage() / timeDiffSeconds;
						
						if (ConfigDynamicDifficulty.difficulty_MaxDPSLoggable != -1 && damage > ConfigDynamicDifficulty.difficulty_MaxDPSLoggable) {
							damage = (float) ConfigDynamicDifficulty.difficulty_MaxDPSLoggable;
						}
						
						log.getListDPSs().add(damage);
						
						//System.out.println("dps log: " + damage + " new Damage: " + event.ammount + " tickDiff: " + timeDiff + " source: " + event.source.damageType + " ID: " + ent.getEntityId());
						
						
					} else {
						//if no time passed, just add last entry onto current entry
						damageToLog += log.getLastDamage();
					}
					
					
				}
				
				log.setLastDamage(damageToLog);
				log.setLastLogTime(world.getTotalWorldTime());
			}
			
			
		}
		
	}
	
	public static void logDeath(LivingDeathEvent event) {
		if (event.getEntity().worldObj.isRemote) return;
		if (ConfigDynamicDifficulty.trackChunkData) {
			
			Entity ent = event.getEntity();
			World world = ent.worldObj;
			
			if (ent instanceof IMob && ent instanceof EntityCreature) {
				if (lookupEntToDamageLog.containsKey(ent.getEntityId())) {
					AttackData log = lookupEntToDamageLog.get(ent.getEntityId());
					logToChunk(log);
					log.cleanup();
					lookupEntToDamageLog.remove(ent.getEntityId());
				}
				
			}
			
		}
	}
	
	public static void logToChunk(AttackData log) {
		int maxShortTermSize = 50;
		int maxLongTermSize = 50;
		int recalcRate = 20*2;
		
		EntityCreature ent = log.getEnt();
		World world = ent.worldObj;
		int chunkX = MathHelper.floor_double(ent.posX / 16);
		int chunkZ = MathHelper.floor_double(ent.posZ / 16);
		ChunkDataPoint cdp = WorldDirectorManager.instance().getChunkDataGrid(world).getChunkData(chunkX, chunkZ);
		
		if (log.getListDPSs().size() == 0 && log.getLastDamage() > 0) {
			//add an insta kill dps that assumes can be done every half second
			float instaKillDPSCalc = log.getLastDamage() * 2;
			
			if (ConfigDynamicDifficulty.difficulty_MaxDPSLoggable != -1 && instaKillDPSCalc > ConfigDynamicDifficulty.difficulty_MaxDPSLoggable) {
				instaKillDPSCalc = (float) ConfigDynamicDifficulty.difficulty_MaxDPSLoggable;
			}
			
			log.getListDPSs().add(instaKillDPSCalc);
		}
		
		if (log.getListDPSs().size() > 0) {
			float avgDPS = 0;
			for (float val : log.getListDPSs()) {
				avgDPS += val;
			}
			avgDPS /= log.getListDPSs().size();
			cdp.listDPSAveragesShortTerm.add(avgDPS);
		}
		
		//trim list
		if (cdp.listDPSAveragesShortTerm.size() > maxShortTermSize) {
			cdp.listDPSAveragesShortTerm.remove(0);
		}
		
		//if time to do a full recalc
		if (cdp.lastDPSRecalc + recalcRate < world.getTotalWorldTime()) {
			
			if (cdp.listDPSAveragesShortTerm.size() > 0) {
				float avgDPS2 = 0;
				for (float val : cdp.listDPSAveragesShortTerm) {
					avgDPS2 += val;
				}
				avgDPS2 /= cdp.listDPSAveragesShortTerm.size();
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
				
				cdp.averageDPS = avgDPS3;
				
				//System.out.println("average of the average of the average: " + avgDPS3);
			}
			
			cdp.lastDPSRecalc = world.getTotalWorldTime();
		}
	}



	public static BuffedLocation buffLocation(World world, BlockCoord coord, int distRadius, float difficulty) {
		BuffedLocation zone = new BuffedLocation(distRadius, difficulty);
		zone.setWorld(world);
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
	
	
	
}
