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
	/*public static String dataListPlayers = "HW_Inv_ActiveTimeStart";
	public static String dataListPlayers = "HW_Inv_ActiveTimeEnd";*/

	/**
	 *
	 * first time init task with time range
	 * reload check if data is there, if not, init as if first time
	 * - dont forget about non invasion mode, factor in dataUseInvasionRules
	 *
	 * - what about if a mob survives between invasions and still has old task
	 * -- do we only enhance mobs that just spawned or do we try to rope in alive ones?
	 *
	 */

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
			if (entity.world.isDaytime()) return false;
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
        IBlockState state = entity.world.getBlockState(pos);
    	if (!entity.world.isAirBlock(pos)) {
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
    	
    	double dist = (double)MathHelper.sqrt(vecX * vecX/* + vecY * vecY*/ + vecZ * vecZ);
    	
    	double scanX = entity.posX + (vecX / dist);
    	double scanZ = entity.posZ + (vecZ / dist);
    	
    	Random rand = new Random(entity.world.getTotalWorldTime());
    	
    	if (rand.nextBoolean()/*Math.abs(vecX) < Math.abs(vecZ)*/) {
    		//scanX = entity.posX;
        	scanZ = entity.posZ + 0;
    	} else {
    		scanX = entity.posX + 0;
        	//scanZ = entity.posZ;
    	}
    	
    	BlockCoord coords = new BlockCoord(MathHelper.floor(scanX), MathHelper.floor(entity.getEntityBoundingBox().minY + 1), MathHelper.floor(scanZ));
    	
    	IBlockState state = entity.world.getBlockState(coords.toBlockPos());
    	Block block = state.getBlock();
    	//Block block = entity.world.getBlock(coords.posX, coords.posY, coords.posZ);
    	
    	//System.out.println("ahead to target: " + block);
    	
    	if (UtilMining.canMineBlock(entity.world, coords, block)) {
			setMiningBlock(state, coords);
    		//entity.world.setBlock(coords.posX, coords.posY, coords.posZ, Blocks.air);
    		return true;
    	} else {
    		if (vecY > 0) {
    			coords.posY++;
    			//coords = coords.add(0, 1, 0);
    			state = entity.world.getBlockState(coords.toBlockPos());
    	    	//block = entity.world.getBlock(coords.posX, coords.posY, coords.posZ);
        		if (UtilMining.canMineBlock(entity.world, coords, block)) {
					setMiningBlock(state, coords);
            		return true;
        		}
    		}
    		
    		//if dont or cant dig up, continue strait
    		coords.posY--;
    		//coords = coords.add(0, -1, 0);
    		
    		state = entity.world.getBlockState(coords.toBlockPos());
	    	block = state.getBlock();
    		if (UtilMining.canMineBlock(entity.world, coords, block)) {
				setMiningBlock(state, coords);
        		return true;
    		} else {
    			//try to dig down if all else failed and target is below
    			if (vecY < 0) {
    				//coords = coords.add(0, -1, 0);
    				coords.posY--;
    	    		state = entity.world.getBlockState(coords.toBlockPos());
    	    		block = state.getBlock();
    	    		//block = entity.world.getBlock(coords.posX, coords.posY, coords.posZ);
    		    	
    	    		if (UtilMining.canMineBlock(entity.world, coords, block)) {
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

		IBlockState state = entity.world.getBlockState(posCurMining.toBlockPos());
		Block block = state.getBlock();
    	
    	//force stop mining if pushed away, or if block changed
    	if (stateCurMining != state || entity.getDistance(posCurMining.posX, posCurMining.posY, posCurMining.posZ) > 3) {
    		//entity.world.destroyBlockInWorldPartially(entity.getEntityId(), posCurMining.posX, posCurMining.posY, posCurMining.posZ, 0);
    		entity.world.sendBlockBreakProgress(entity.getEntityId(), posCurMining.toBlockPos(), 0);
			setMiningBlock(null, null);
    		return;
    	}
    	
    	entity.getNavigator().clearPathEntity();
    	
    	//Block block = entity.world.getBlock(posCurMining.posX, posCurMining.posY, posCurMining.posZ);
    	//double blockStrength = block.getBlockHardness(entity.world, posCurMining.posX, posCurMining.posY, posCurMining.posZ);
    	//Block block = state.getBlock();

    	
    	double blockStrength = state.getBlockHardness(entity.world, posCurMining.toBlockPos());
    	
    	if (blockStrength == -1) {
			setMiningBlock(null, null);
    		return;
    	}


		if (entity.world.getTotalWorldTime() % 10 == 0) {
			//entity.swingItem();
			entity.swingArm(EnumHand.MAIN_HAND);
			//System.out.println("swing!");

			entity.world.playSound(null, new BlockPos(posCurMining.getX(), posCurMining.getY(), posCurMining.getZ()), block.getSoundType(state, entity.world, posCurMining.toBlockPos(), entity).getBreakSound(), SoundCategory.HOSTILE, 0.5F, 1F);
			//entity.world.playSoundEffect(posCurMining.getX(), posCurMining.getY(), posCurMining.getZ(), block.stepSound.getBreakSound(), 0.5F, 1F);
		}
    	
    	curBlockDamage += 0.01D / blockStrength;
    	
    	if (curBlockDamage > 1D) {
    		//entity.world.destroyBlockInWorldPartially(entity.getEntityId(), posCurMining.posX, posCurMining.posY, posCurMining.posZ, 0);
    		entity.world.sendBlockBreakProgress(entity.getEntityId(), posCurMining.toBlockPos(), 0);
    		//entity.world.setBlock(posCurMining.posX, posCurMining.posY, posCurMining.posZ, Blocks.AIR);
    		//entity.world.setBlockToAir(posCurMining.toBlockPos());
            if (UtilMining.canConvertToRepairingBlock(entity.world, state)) {
                TileEntityRepairingBlock.replaceBlockAndBackup(entity.world, posCurMining.toBlockPos());
            } else {
                Block.spawnAsEntity(entity.world, posCurMining.toBlockPos(), new ItemStack(state.getBlock(), 1));
				entity.world.setBlockToAir(posCurMining.toBlockPos());
            }

			setMiningBlock(null, null);

            curBlockDamage = 0;
    		
    	} else {
    		//entity.world.destroyBlockInWorldPartially(entity.getEntityId(), posCurMining.posX, posCurMining.posY, posCurMining.posZ, (int)(curBlockDamage * 10D));
    		entity.world.sendBlockBreakProgress(entity.getEntityId(), posCurMining.toBlockPos(), (int)(curBlockDamage * 10D));
    	}
    }
}
