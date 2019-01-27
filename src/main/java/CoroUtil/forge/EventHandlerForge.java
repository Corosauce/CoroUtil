package CoroUtil.forge;

import CoroUtil.ai.IInvasionControlledTask;
import CoroUtil.block.TileEntityRepairingBlock;
import CoroUtil.config.ConfigCoroUtilAdvanced;
import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.difficulty.DynamicDifficulty;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.buffs.BuffBase;
import CoroUtil.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.test.Headshots;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.grid.block.BlockDataPoint;
import CoroUtil.world.grid.chunk.ChunkDataPoint;

import java.util.Iterator;
import java.util.List;

public class EventHandlerForge {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void deathEvent(LivingDeathEvent event) {

		if (event.isCanceled()) return;

		PlayerQuestManager.i().onEvent(event);

		if (CoroUtilCompatibility.isHWInvasionsInstalled()) {
			if (!event.getEntity().world.isRemote) {
				if (event.getEntity() instanceof EntityPlayer) {
					DynamicDifficulty.deathPlayer((EntityPlayer) event.getEntity());

					//also remove invasion skip buff since the invaders got what they wanted (also covers edge case of player removing invasion mod and buff remaining)
					DynamicDifficulty.setInvasionSkipBuff((EntityPlayer) event.getEntity(), 0);
					//event.getEntity().getEntityData().setFloat(DynamicDifficulty.dataPlayerInvasionSkipBuff, 0);
				}

				UtilEntityBuffs.onDeath(event);
			}
		}
	}
	
	@SubscribeEvent
	public void pickupEvent(EntityItemPickupEvent event) {
		PlayerQuestManager.i().onEvent(event);
	}
	
	@SubscribeEvent
	public void worldSave(Save event) {
		
		//this is called for every dimension
		//check server side because some mods invoke saving client side (bad standard)
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			if (((WorldServer)event.getWorld()).provider.getDimension() == 0) {
				CoroUtil.writeOutData(false);
			}
		}
	}
	
	@SubscribeEvent
	public void worldLoad(Load event) {
		if (!event.getWorld().isRemote) {
			if (((WorldServer)event.getWorld()).provider.getDimension() == 0) {
				if (WorldDirectorManager.instance().getWorldDirector(CoroUtil.modID, event.getWorld()) == null) {
					WorldDirectorManager.instance().registerWorldDirector(new WorldDirector(true), CoroUtil.modID, event.getWorld());
				}
			}
		}
	}
	
	@SubscribeEvent
	public void breakBlockHarvest(HarvestDropsEvent event) {
		PlayerQuestManager.i().onEvent(event);
		DynamicDifficulty.handleHarvest(event);
	}
	
	@SubscribeEvent
	public void breakBlockPlayer(BreakEvent event) {
		PlayerQuestManager.i().onEvent(event);
	}
	
	@SubscribeEvent
	public void blockPlayerInteract(PlayerInteractEvent event) {
		if (!event.getWorld().isRemote) {
			try {
				
				//an event is fired where its air and has no chunk X or Z, cancel this
				//if (event.action == Action.RIGHT_CLICK_AIR) return;
				if (event instanceof RightClickEmpty) return;
				
				if (ConfigCoroUtilAdvanced.trackPlayerData) {
					ChunkDataPoint cdp = WorldDirectorManager.instance().getChunkDataGrid(event.getWorld()).getChunkData(event.getPos().getX() / 16, event.getPos().getZ() / 16);
					cdp.addToPlayerActivityInteract(event.getEntityPlayer().getGameProfile().getId(), 1);
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	@SubscribeEvent
	public void entityHurt(LivingHurtEvent event) {
		Headshots.hookLivingHurt(event);
		DynamicDifficulty.logDamage(event);
	}
	
	@SubscribeEvent
	public void entityKilled(LivingDeathEvent event) {
		DynamicDifficulty.logDeath(event);
	}
	
	@SubscribeEvent
	public void entityTick(LivingUpdateEvent event) {
		
		EntityLivingBase ent = event.getEntityLiving();
		if (!ent.world.isRemote) {
			if (ent instanceof EntityPlayer) {
				CoroUtilPlayer.trackPlayerForSpeed((EntityPlayer) ent);
			}
		}
		
		if (ConfigCoroUtilAdvanced.desirePathDerp) {
			
			int walkOnRate = 5;
			
			if (!ent.world.isRemote) {
				if (ent.world.getTotalWorldTime() % walkOnRate == 0) {
					double speed = Math.sqrt(ent.motionX * ent.motionX + ent.motionY * ent.motionY + ent.motionZ * ent.motionZ);
					if (ent instanceof EntityPlayer) {
						Vec3 vec = CoroUtilPlayer.getPlayerSpeedCapped((EntityPlayer) ent, 0.1F);
						speed = Math.sqrt(vec.xCoord * vec.xCoord + vec.yCoord * vec.yCoord + vec.zCoord * vec.zCoord);
					}
					if (speed > 0.08) {
						//System.out.println(entityId + " - speed: " + speed);
						int newX = MathHelper.floor(ent.posX);
						int newY = MathHelper.floor(ent.getEntityBoundingBox().minY - 1);
						int newZ = MathHelper.floor(ent.posZ);
						IBlockState state = ent.world.getBlockState(new BlockPos(newX, newY, newZ));
						Block id = state.getBlock();
						
						//check for block that can have beaten path data
						
						if (id == Blocks.GRASS) {
							BlockDataPoint bdp = WorldDirectorManager.instance().getBlockDataGrid(ent.world).getBlockData(newX, newY, newZ);// ServerTickHandler.wd.getBlockDataGrid(worldObj).getBlockData(newX, newY, newZ);
							
							//add depending on a weight?
							bdp.walkedOnAmount += 0.25F;
							
							//System.out.println("inc walk amount: " + bdp.walkedOnAmount);
							
							if (bdp.walkedOnAmount > 5F) {
								//System.out.println("dirt!!!");
								if (ent.world.getBlockState(new BlockPos(newX, newY+1, newZ)).getBlock() == Blocks.AIR) {
									ent.world.setBlockState(new BlockPos(newX, newY, newZ), Blocks.GRASS_PATH.getDefaultState());
								}

								//reset walked on amount since its a new block state
                                bdp.walkedOnAmount = 0;

								//cleanup for memory if we can
								WorldDirectorManager.instance().getBlockDataGrid(ent.world).removeBlockDataIfRemovable(newX, newY, newZ);
							}
						}
					}
				}
			}
		}

		//remove tasks that are marked to be removed
		if (!ent.world.isRemote) {
			if ((ent.world.getTotalWorldTime() + ent.getEntityId()) % 20 == 0) {

				//NOTE: this code doesnt actually know if its an invasion, it just assumes if it has the buff and its now daytime, invasion is over
				if (ConfigCoroUtilAdvanced.removeInvasionAIWhenInvasionDone && ent.getEntityData().getBoolean(UtilEntityBuffs.dataEntityBuffed)) {

					//persistance management here too maybe?
					//invasion mod preventing this via AllowDespawn event in its handler class

					if (ent instanceof EntityLiving) {
						EntityLiving entL = (EntityLiving) ent;
						Iterator<EntityAITasks.EntityAITaskEntry> it = entL.tasks.taskEntries.iterator();
						while (it.hasNext()) {
							EntityAITasks.EntityAITaskEntry task = it.next();
							if (task.action instanceof IInvasionControlledTask) {
								if (((IInvasionControlledTask) task.action).shouldBeRemoved()) {
									//entL.tasks.removeTask(task.action);
									task.action.resetTask();
									it.remove();
								}
							}
						}

						it = entL.targetTasks.taskEntries.iterator();
						while (it.hasNext()) {
							EntityAITasks.EntityAITaskEntry task = it.next();
							if (task.action instanceof IInvasionControlledTask) {
								if (((IInvasionControlledTask) task.action).shouldBeRemoved()) {
									//entL.targetTasks.removeTask(task.action);
									task.action.resetTask();
									it.remove();
								}
							}
						}
					}
				}
			}


			if (ent.getEntityData().getCompoundTag(UtilEntityBuffs.dataEntityBuffed_Data).getBoolean(UtilEntityBuffs.dataEntityBuffed_AI_Digging)) {
//trying to get miners to push others out of the way
				boolean pushMobsAwayForMiners = true;
				if (pushMobsAwayForMiners) {

					List<Entity> list = ent.world.getEntitiesInAABBexcluding(ent, ent.getEntityBoundingBox().grow(0.5, 0.5, 0.5), EntitySelectors.getTeamCollisionPredicate(ent));

					if (!list.isEmpty())
					{

						for (int l = 0; l < list.size(); ++l)
						{
							Entity entityIn = list.get(l);

							//from applyEntityCollision()

							NBTTagCompound data2 = entityIn.getEntityData().getCompoundTag(UtilEntityBuffs.dataEntityBuffed_Data);


							if (entityIn instanceof EntityLiving && !ent.isRidingSameEntity(entityIn) && !data2.getBoolean(UtilEntityBuffs.dataEntityBuffed_AI_Digging))
							{
								if (!entityIn.noClip && !ent.noClip)
								{
									double d0 = entityIn.posX - ent.posX;
									double d1 = entityIn.posZ - ent.posZ;
									double d2 = MathHelper.absMax(d0, d1);

									if (d2 >= 0.009999999776482582D)
									{
										d2 = (double)MathHelper.sqrt(d2);
										d0 = d0 / d2;
										d1 = d1 / d2;
										double d3 = 1.0D / d2;

										if (d3 > 1.0D)
										{
											d3 = 1.0D;
										}

										d0 = d0 * d3;
										d1 = d1 * d3;
										d0 = d0 * 0.10D;
										d1 = d1 * 0.10D;
										d0 = d0 * (double)(1.0F - ent.entityCollisionReduction);
										d1 = d1 * (double)(1.0F - ent.entityCollisionReduction);

										if (!ent.isBeingRidden())
										{
											entityIn.addVelocity(d0, 0.0D, d1);
										}

										if (!entityIn.isBeingRidden())
										{
											entityIn.addVelocity(d0, 0.0D, d1);
										}
									}
								}
							}

						}
					}

				}

				//cancel out water motion for miners
				//copied from vec using code copied World.handleMaterialAcceleration
				//perfectly cancels flow but only when their center is actually in the water
				boolean waterFixCancelPush = true;
				if (waterFixCancelPush) {
					if (ent.isInWater()) {
						//backup motion
						double motionXOld = ent.motionX;
						double motionYOld = ent.motionY;
						double motionZOld = ent.motionZ;
						//remove motion so only difference is material influenced
						ent.motionX = 0;
						ent.motionY = 0;
						ent.motionZ = 0;
						ent.world.handleMaterialAcceleration(ent.getEntityBoundingBox(), Material.WATER, ent);
						//get changes
						double motionXChange = ent.motionX;
						double motionYChange = ent.motionY;
						double motionZChange = ent.motionZ;
						//apply old motion with inverted material influence
						ent.motionX = motionXOld + (motionXChange * -1);
						ent.motionY = motionYOld + (motionYChange * -1);
						ent.motionZ = motionZOld + (motionZChange * -1);
					}
				}

				boolean waterFixLeapOut = true;
				if (waterFixLeapOut) {
					String nbtID = "CoroUtil_wasInWater";
					if (ent.getEntityData().getBoolean(nbtID)) {
						if (!ent.isInWater()) {
							if (ent.isCollidedHorizontally) {
								ent.motionY += 0.4F;
							}
						}
					}

					ent.getEntityData().setBoolean(nbtID, ent.isInWater());
				}
			}







		}
	}

	/**
	 * Called on load from chunk, or spawnEntityInWorld, so do first time effects after spawning in and there will be no double buffs
	 *
	 * @param event
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void entityCreated(EntityJoinWorldEvent event) {
		if (event.getEntity().world.isRemote) return;
		//System.out.println("coroutil event EntityJoinWorldEvent for " + event.getEntity());

		CoroUtilCrossMod.processSpawnOverride(event);

		if (event.getEntity() instanceof EntityCreature) {
			EntityCreature ent = (EntityCreature) event.getEntity();

			//if buffed and was not literally just spawned (prevents duplicate buff applying from invasion spawning + this code)
			if (ent.getEntityData().getBoolean(UtilEntityBuffs.dataEntityBuffed) && !ent.getEntityData().getBoolean(UtilEntityBuffs.dataEntityInitialSpawn)) {
				float difficultySpawnedIn = 0;
				if (ent.getEntityData().hasKey(UtilEntityBuffs.dataEntityBuffed_Difficulty)) {
					difficultySpawnedIn = ent.getEntityData().getFloat(UtilEntityBuffs.dataEntityBuffed_Difficulty);
				} else {
					//safely get difficulty for area
					if (ent.world.isBlockLoaded(ent.getPosition())) {
						difficultySpawnedIn = DynamicDifficulty.getDifficultyAveragedForArea(ent);
					}
				}

				List<String> buffs = UtilEntityBuffs.getAllBuffNames();
				NBTTagCompound data = ent.getEntityData().getCompoundTag(UtilEntityBuffs.dataEntityBuffed_Data);
				for (String buff : buffs) {
					if (data.getBoolean(buff)) {
						BuffBase buffObj = UtilEntityBuffs.getBuff(buff);
						if (buffObj != null) {
							//System.out.println("reloading buff: " + buff);
							CULog.dbg("applyBuffFromReload: " + buff);
							buffObj.applyBuffFromReload(ent, difficultySpawnedIn);
						} else {
							CoroUtil.dbg("warning: unable to find buff by name of " + buff);
						}
					}
				}

				UtilEntityBuffs.applyBuffPostAll(ent, difficultySpawnedIn);
			}
		}
	}

	//use lowest to make sure FTBU claimed chunks take priority and do their work first
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void explosionEvent(ExplosionEvent.Detonate event) {

		if (event.getWorld().isRemote) return;

		if (ConfigHWMonsters.explosionsTurnIntoRepairingBlocks || ConfigHWMonsters.explosionsDontDestroyTileEntities) {
			//since we currently dont support dealing with them, just prevent them from breaking at all
			boolean protectTileEntities = true;
			List<BlockPos> listPos = event.getExplosion().getAffectedBlockPositions();

			//listPos.forEach(() -> );
			for (Iterator<BlockPos> it = listPos.iterator(); it.hasNext();) {
				BlockPos pos = it.next();
				if (ConfigHWMonsters.explosionsDontDestroyTileEntities && event.getWorld().getTileEntity(pos) != null) {
					it.remove();
				} else if (ConfigHWMonsters.explosionsTurnIntoRepairingBlocks) {
					IBlockState state = event.getWorld().getBlockState(pos);
					if (UtilMining.canMineBlock(event.getWorld(), pos, state.getBlock()) &&
							UtilMining.canConvertToRepairingBlock(event.getWorld(), state)) {
						TileEntityRepairingBlock.replaceBlockAndBackup(event.getWorld(), pos);
					}

					//always protect when this setting on, either its replaced (block) or not destroyed (tile entity)
					it.remove();
				}
			}

			//event.getExplosion().clearAffectedBlockPositions();
		}
	}
}
