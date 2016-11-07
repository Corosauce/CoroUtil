package CoroUtil.forge;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import CoroUtil.config.ConfigCoroAI;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.util.CoroUtilPlayer;
import CoroUtil.util.Vec3;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.grid.block.BlockDataPoint;
import CoroUtil.world.grid.chunk.ChunkDataPoint;
import CoroUtil.world.player.DynamicDifficulty;

public class EventHandlerForge {

	@SubscribeEvent
	public void deathEvent(LivingDeathEvent event) {
		PlayerQuestManager.i().onEvent(event);
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
		DynamicDifficulty.logDamage(event);
	}
	
	@SubscribeEvent
	public void entityKilled(LivingDeathEvent event) {
		DynamicDifficulty.logDeath(event);
	}
	
	@SubscribeEvent
	public void entityTick(LivingUpdateEvent event) {
		
		EntityLivingBase ent = event.getEntityLiving();
		if (!ent.worldObj.isRemote) {
			if (ent instanceof EntityPlayer) {
				CoroUtilPlayer.trackPlayerForSpeed((EntityPlayer) ent);
			}
		}
		
		if (ConfigCoroAI.desirePathDerp) {
			
			int walkOnRate = 5;
			
			if (!ent.worldObj.isRemote) {
				if (ent.worldObj.getTotalWorldTime() % walkOnRate == 0) {
					double speed = Math.sqrt(ent.motionX * ent.motionX + ent.motionY * ent.motionY + ent.motionZ * ent.motionZ);
					if (ent instanceof EntityPlayer) {
						Vec3 vec = CoroUtilPlayer.getPlayerSpeedCapped((EntityPlayer) ent, 0.1F);
						speed = Math.sqrt(vec.xCoord * vec.xCoord + vec.yCoord * vec.yCoord + vec.zCoord * vec.zCoord);
					}
					if (speed > 0.08) {
						//System.out.println(entityId + " - speed: " + speed);
						int newX = MathHelper.floor_double(ent.posX);
						int newY = MathHelper.floor_double(ent.getEntityBoundingBox().minY - 1);
						int newZ = MathHelper.floor_double(ent.posZ);
						IBlockState state = ent.worldObj.getBlockState(new BlockPos(newX, newY, newZ));
						Block id = state.getBlock();
						
						//check for block that can have beaten path data
						
						if (id == Blocks.GRASS) {
							BlockDataPoint bdp = WorldDirectorManager.instance().getBlockDataGrid(ent.worldObj).getBlockData(newX, newY, newZ);// ServerTickHandler.wd.getBlockDataGrid(worldObj).getBlockData(newX, newY, newZ);
							
							//add depending on a weight?
							bdp.walkedOnAmount += 0.25F;
							
							//System.out.println("inc walk amount: " + bdp.walkedOnAmount);
							
							if (bdp.walkedOnAmount > 5F) {
								//System.out.println("dirt!!!");
								if (ent.worldObj.getBlockState(new BlockPos(newX, newY+1, newZ)).getBlock() == Blocks.AIR) {
									ent.worldObj.setBlockState(new BlockPos(newX, newY, newZ), Blocks.GRASS_PATH.getDefaultState());
								}
								
								//BlockRegistry.dirtPath.blockID);
								//cleanup for memory
								WorldDirectorManager.instance().getBlockDataGrid(ent.worldObj).removeBlockData(newX, newY, newZ);
								//ServerTickHandler.wd.getBlockDataGrid(worldObj).removeBlockData(newX, newY, newZ);
							}
						}
					}
				}
			}
		}
	}
}
