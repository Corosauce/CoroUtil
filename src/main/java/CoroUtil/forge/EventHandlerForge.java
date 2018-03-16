package CoroUtil.forge;

import CoroUtil.difficulty.DynamicDifficulty;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.buffs.BuffBase;
import CoroUtil.util.CoroUtilCrossMod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import CoroUtil.config.ConfigCoroAI;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.test.Headshots;
import CoroUtil.util.CoroUtilPlayer;
import CoroUtil.util.Vec3;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.grid.block.BlockDataPoint;
import CoroUtil.world.grid.chunk.ChunkDataPoint;

import java.util.List;

public class EventHandlerForge {

	@SubscribeEvent
	public void deathEvent(LivingDeathEvent event) {

		if (event.isCanceled()) return;

		PlayerQuestManager.i().onEvent(event);

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
				
				if (ConfigCoroAI.trackPlayerData) {
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
		
		if (ConfigCoroAI.desirePathDerp) {
			
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
								
								//BlockRegistry.dirtPath.blockID);
								//cleanup for memory
								WorldDirectorManager.instance().getBlockDataGrid(ent.world).removeBlockData(newX, newY, newZ);
								//ServerTickHandler.wd.getBlockDataGrid(worldObj).removeBlockData(newX, newY, newZ);
							}
						}
					}
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

			if (ent.getEntityData().getBoolean(UtilEntityBuffs.dataEntityBuffed)) {
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
}
