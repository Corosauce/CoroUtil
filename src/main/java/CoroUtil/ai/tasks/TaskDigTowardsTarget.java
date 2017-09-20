package CoroUtil.ai.tasks;

import java.util.Random;

import CoroUtil.block.TileEntityRepairingBlock;
import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.forge.CommonProxy;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilPlayer;
import CoroUtil.util.UtilMining;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import CoroUtil.ai.ITaskInitializer;
import CoroUtil.util.BlockCoord;

public class TaskDigTowardsTarget extends EntityAIBase implements ITaskInitializer
{
    private EntityCreature entity = null;
	private IBlockState stateCurMining = null;
    private BlockCoord posCurMining = null;
    private EntityLivingBase targetLastTracked = null;
    private int digTimeCur = 0;
    private int digTimeMax = 15*20;
    private double curBlockDamage = 0D;
    //doesnt factor in ai tick delay of % 3
    private int noMoveTicks = 0;

	/**
	 * Fields used when task is added via invasions, but not via hwmonsters, or do we want that too?
	 */
	public static String dataUseInvasionRules = "HW_Inv_UseInvasionRules";
	public static String dataUsePlayerList = "HW_Inv_UsePlayerList";
	public static String dataWhitelistMode = "HW_Inv_WhitelistMode";
	public static String dataListPlayers = "HW_Inv_ListPlayers";

    public TaskDigTowardsTarget()
    {
        //this.setMutexBits(3);
    }
    
    @Override
    public void setEntity(EntityCreature creature) {
    	this.entity = creature;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
    	//this method ticks every 3 ticks in best conditions
		boolean forInvasion = entity.getEntityData().getBoolean(dataUseInvasionRules);
    	
    	//prevent day digging, easy way to prevent digging once invasion ends
		if (forInvasion) {
			if (entity.worldObj.isDaytime()) return false;
		}
    	
    	//System.out.println("should?");
    	/**
    	 * Zombies wouldnt try to mine if they are bunched up behind others, as they are still technically pathfinding, this helps resolve that issue, and maybe water related issues
    	 */
    	double movementThreshold = 0.05D;
    	int noMoveThreshold = 5;
    	if (posCurMining == null && entity.motionX < movementThreshold && entity.motionX > -movementThreshold && 
    			entity.motionZ < movementThreshold && entity.motionZ > -movementThreshold) {
    		
    		noMoveTicks++;
    		
    	} else {
    		noMoveTicks = 0;
    	}
    	
    	//System.out.println("noMoveTicks: " + noMoveTicks);
    	/*if (noMoveTicks > noMoveThreshold) {
    		System.out.println("ent not moving enough, try to mine!? " + noMoveTicks + " ent: " + entity.getEntityId());
    	}*/
    	
    	if (!entity.onGround && !entity.isInWater()) return false;
    	//return true if not pathing, has target
    	if (entity.getAttackTarget() != null || targetLastTracked != null) {
    		if (entity.getAttackTarget() == null) {
    			//System.out.println("forcing reset of target2");
    			entity.setAttackTarget(targetLastTracked);
				//fix for scenario where setAttackTarget calls forge event and someone undoes target setting
				if (entity.getAttackTarget() == null) {
					return false;
				}
    		} else {
    			targetLastTracked = entity.getAttackTarget();
    		}

    		//prevent invasion spawned diggers to not dig for players with invasions off
			if (entity.getEntityData().getBoolean(dataUsePlayerList)) {
				String playerName = CoroUtilEntity.getName(entity.getAttackTarget());
				boolean whitelistMode = entity.getEntityData().getBoolean(dataWhitelistMode);
				String listPlayers = entity.getEntityData().getString(dataListPlayers);

				if (whitelistMode) {
					if (!listPlayers.contains(playerName)) {
						return false;
					}
				} else {
					if (listPlayers.contains(playerName)) {
						return false;
					}
				}
			}

    		//if (!entity.getNavigator().noPath()) System.out.println("path size: " + entity.getNavigator().getPath().getCurrentPathLength());
    		if (entity.getNavigator().noPath() || entity.getNavigator().getPath().getCurrentPathLength() == 1 || noMoveTicks > noMoveThreshold) {
    		//if (entity.motionX < 0.1D && entity.motionZ < 0.1D) {
    			if (updateBlockToMine()) {
    				//System.out.println("should!");
    				return true;
    			}
    		} else {
    			//clause for if stuck trying to path
    			
    		}
    	}
    	
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
    	//System.out.println("continue!");
    	if (posCurMining == null) return false;
        BlockPos pos = new BlockPos(posCurMining.posX, posCurMining.posY, posCurMining.posZ);
        IBlockState state = entity.worldObj.getBlockState(pos);
    	if (!entity.worldObj.isAirBlock(pos)) {
    		return true;
    	} else {
			setMiningBlock(null, null);
    		//System.out.println("ending execute");
    		return false;
    	}
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
    	//System.out.println("start!");
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
    	//System.out.println("reset!");
    	digTimeCur = 0;
    	curBlockDamage = 0;
		setMiningBlock(null, null);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
    	//System.out.println("running!");
    	
    	if (entity.getAttackTarget() != null) {
    		targetLastTracked = entity.getAttackTarget();
    	} else {
    		if (targetLastTracked != null) {
    			//System.out.println("forcing reset of target");
    			entity.setAttackTarget(targetLastTracked);
    		}
    	}
    	
    	tickMineBlock();
    }
    
    public boolean updateBlockToMine() {

		//fix for scenario where setAttackTarget calls forge event and someone undoes target setting
		if (entity.getAttackTarget() == null) {
			return false;
		}

		setMiningBlock(null, null);
    	
    	double vecX = entity.getAttackTarget().posX - entity.posX;
    	//feet
    	double vecY = entity.getAttackTarget().posY - entity.getEntityBoundingBox().minY;
    	double vecZ = entity.getAttackTarget().posZ - entity.posZ;
    	
    	double dist = (double)MathHelper.sqrt_double(vecX * vecX/* + vecY * vecY*/ + vecZ * vecZ);
    	
    	double scanX = entity.posX + (vecX / dist);
    	double scanZ = entity.posZ + (vecZ / dist);
    	
    	Random rand = new Random(entity.worldObj.getTotalWorldTime());
    	
    	if (rand.nextBoolean()/*Math.abs(vecX) < Math.abs(vecZ)*/) {
    		//scanX = entity.posX;
        	scanZ = entity.posZ + 0;
    	} else {
    		scanX = entity.posX + 0;
        	//scanZ = entity.posZ;
    	}
    	
    	BlockCoord coords = new BlockCoord(MathHelper.floor_double(scanX), MathHelper.floor_double(entity.getEntityBoundingBox().minY + 1), MathHelper.floor_double(scanZ));
    	
    	IBlockState state = entity.worldObj.getBlockState(coords.toBlockPos());
    	Block block = state.getBlock();
    	//Block block = entity.worldObj.getBlock(coords.posX, coords.posY, coords.posZ);
    	
    	//System.out.println("ahead to target: " + block);
    	
    	if (UtilMining.canMineBlock(entity.worldObj, coords, block)) {
			setMiningBlock(state, coords);
    		//entity.worldObj.setBlock(coords.posX, coords.posY, coords.posZ, Blocks.air);
    		return true;
    	} else {
    		if (vecY > 0) {
    			coords.posY++;
    			//coords = coords.add(0, 1, 0);
    			state = entity.worldObj.getBlockState(coords.toBlockPos());
    	    	//block = entity.worldObj.getBlock(coords.posX, coords.posY, coords.posZ);
        		if (UtilMining.canMineBlock(entity.worldObj, coords, block)) {
					setMiningBlock(state, coords);
            		return true;
        		}
    		}
    		
    		//if dont or cant dig up, continue strait
    		coords.posY--;
    		//coords = coords.add(0, -1, 0);
    		
    		state = entity.worldObj.getBlockState(coords.toBlockPos());
	    	block = state.getBlock();
    		if (UtilMining.canMineBlock(entity.worldObj, coords, block)) {
				setMiningBlock(state, coords);
        		return true;
    		} else {
    			//try to dig down if all else failed and target is below
    			if (vecY < 0) {
    				//coords = coords.add(0, -1, 0);
    				coords.posY--;
    	    		state = entity.worldObj.getBlockState(coords.toBlockPos());
    	    		block = state.getBlock();
    	    		//block = entity.worldObj.getBlock(coords.posX, coords.posY, coords.posZ);
    		    	
    	    		if (UtilMining.canMineBlock(entity.worldObj, coords, block)) {
						setMiningBlock(state, coords);
    	        		return true;
    	    		}
    			}
    		}
    		
    		return false;
    	}
    }

    public void setMiningBlock(IBlockState state, BlockCoord pos) {
		this.posCurMining = pos;
		this.stateCurMining = state;
	}
    
    public void tickMineBlock() {
    	if (posCurMining == null) return;

		IBlockState state = entity.worldObj.getBlockState(posCurMining.toBlockPos());
		Block block = state.getBlock();
    	
    	//force stop mining if pushed away, or if block changed
    	if (stateCurMining != state || entity.getDistance(posCurMining.posX, posCurMining.posY, posCurMining.posZ) > 3) {
    		//entity.worldObj.destroyBlockInWorldPartially(entity.getEntityId(), posCurMining.posX, posCurMining.posY, posCurMining.posZ, 0);
    		entity.worldObj.sendBlockBreakProgress(entity.getEntityId(), posCurMining.toBlockPos(), 0);
			setMiningBlock(null, null);
    		return;
    	}
    	
    	entity.getNavigator().clearPathEntity();
    	
    	//Block block = entity.worldObj.getBlock(posCurMining.posX, posCurMining.posY, posCurMining.posZ);
    	//double blockStrength = block.getBlockHardness(entity.worldObj, posCurMining.posX, posCurMining.posY, posCurMining.posZ);
    	//Block block = state.getBlock();

    	
    	double blockStrength = state.getBlockHardness(entity.worldObj, posCurMining.toBlockPos());
    	
    	if (blockStrength == -1) {
			setMiningBlock(null, null);
    		return;
    	}


		if (entity.worldObj.getTotalWorldTime() % 10 == 0) {
			//entity.swingItem();
			entity.swingArm(EnumHand.MAIN_HAND);
			//System.out.println("swing!");

			entity.worldObj.playSound(null, new BlockPos(posCurMining.getX(), posCurMining.getY(), posCurMining.getZ()), block.getSoundType(state, entity.worldObj, posCurMining.toBlockPos(), entity).getBreakSound(), SoundCategory.HOSTILE, 0.5F, 1F);
			//entity.worldObj.playSoundEffect(posCurMining.getX(), posCurMining.getY(), posCurMining.getZ(), block.stepSound.getBreakSound(), 0.5F, 1F);
		}
    	
    	curBlockDamage += 0.01D / blockStrength;
    	
    	if (curBlockDamage > 1D) {
    		//entity.worldObj.destroyBlockInWorldPartially(entity.getEntityId(), posCurMining.posX, posCurMining.posY, posCurMining.posZ, 0);
    		entity.worldObj.sendBlockBreakProgress(entity.getEntityId(), posCurMining.toBlockPos(), 0);
    		//entity.worldObj.setBlock(posCurMining.posX, posCurMining.posY, posCurMining.posZ, Blocks.AIR);
    		//entity.worldObj.setBlockToAir(posCurMining.toBlockPos());
            if (UtilMining.canConvertToRepairingBlock(entity.worldObj, state)) {
                entity.worldObj.setBlockState(posCurMining.toBlockPos(), CommonProxy.blockRepairingBlock.getDefaultState());
                TileEntity tEnt = entity.worldObj.getTileEntity(posCurMining.toBlockPos());
                if (tEnt instanceof TileEntityRepairingBlock) {
                    ((TileEntityRepairingBlock) tEnt).setBlockData(state);
                }
            } else {
                Block.spawnAsEntity(entity.worldObj, posCurMining.toBlockPos(), new ItemStack(state.getBlock(), 1));
				entity.worldObj.setBlockToAir(posCurMining.toBlockPos());
            }

			setMiningBlock(null, null);
    		
    	} else {
    		//entity.worldObj.destroyBlockInWorldPartially(entity.getEntityId(), posCurMining.posX, posCurMining.posY, posCurMining.posZ, (int)(curBlockDamage * 10D));
    		entity.worldObj.sendBlockBreakProgress(entity.getEntityId(), posCurMining.toBlockPos(), (int)(curBlockDamage * 10D));
    	}
    }
}
