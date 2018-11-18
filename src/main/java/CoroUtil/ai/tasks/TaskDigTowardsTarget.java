package CoroUtil.ai.tasks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import CoroUtil.ai.IInvasionControlledTask;
import CoroUtil.block.TileEntityRepairingBlock;
import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.forge.CULog;
import CoroUtil.forge.CommonProxy;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilPlayer;
import CoroUtil.util.UtilMining;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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

public class TaskDigTowardsTarget extends EntityAIBase implements ITaskInitializer, IInvasionControlledTask
{
    private EntityCreature entity = null;
	private IBlockState stateCurMining = null;
    private BlockCoord posCurMining = null;
    private EntityLivingBase targetLastTracked = null;
    private int digTimeCur = 0;
    private int digTimeMax = 15*20;
    private double curBlockDamage = 0D;
    private int noMoveTicks = 0;
    private ArrayDeque<BlockPos> listPillarToMine = new ArrayDeque<>();

    public boolean debug = true;

	/**
	 * Fields used when task is added via invasions, but not via hwmonsters, or do we want that too?
	 */
	public static String dataUseInvasionRules = "HW_Inv_UseInvasionRules";
	public static String dataUsePlayerList = "HW_Inv_UsePlayerList";
	public static String dataWhitelistMode = "HW_Inv_WhitelistMode";
	public static String dataListPlayers = "HW_Inv_ListPlayers";
	/*public static String dataListPlayers = "HW_Inv_ActiveTimeStart";
	public static String dataListPlayers = "HW_Inv_ActiveTimeEnd";*/

    public TaskDigTowardsTarget()
    {
        this.setMutexBits(3);
    }
    
    @Override
    public void setEntity(EntityCreature creature) {
    	this.entity = creature;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
	public boolean shouldExecute()
    {
    	//dbg("should?");
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
	@Override
    public boolean shouldContinueExecuting()
    {
		//dbg("continue?");
    	if (posCurMining == null) {
			dbg("shouldContinueExecuting fail because posCurMining == null");
    		return false;
		}
        BlockPos pos = new BlockPos(posCurMining.posX, posCurMining.posY, posCurMining.posZ);
        IBlockState state = entity.world.getBlockState(pos);
    	if (!entity.world.isAirBlock(pos)) {
    		return true;
    	} else {
			setMiningBlock(null, null);
			dbg("shouldContinueExecuting fail because not air");
    		//System.out.println("ending execute");
    		return false;
    	}
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
	@Override
    public void startExecuting()
    {
    	dbg("start mining task");
    	//System.out.println("start!");
    }

    /**
     * Resets the task
     */
	@Override
    public void resetTask()
    {
    	//System.out.println("reset!");
		//Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
    	digTimeCur = 0;
    	curBlockDamage = 0;
    	listPillarToMine.clear();
		setMiningBlock(null, null);
    }

    /**
     * Updates the task
     */

    @Override
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

		double entPosX = Math.floor(entity.posX) + 0.5F;
		double entPosZ = Math.floor(entity.posZ) + 0.5F;
    	
    	double vecX = entity.getAttackTarget().posX - entPosX;
    	//feet
    	double vecY = entity.getAttackTarget().getEntityBoundingBox().minY - entity.getEntityBoundingBox().minY;
    	double vecZ = entity.getAttackTarget().posZ - entPosZ;

    	//get angle, snap it to 90, then reconvert back to scalar which is now locked to best 90 degree option
		double angle = Math.atan2(vecZ, vecX);
		angle = Math.round(Math.toDegrees(angle) / 90) * 90;
		//verified this matched the original scalar version of vecX / Z vars before it was snapped to 90
		double relX = Math.cos(angle);
		double relZ = Math.sin(angle);
    	
    	double scanX = entPosX + relX;
    	double scanZ = entPosZ + relZ;

		double distHoriz = Math.sqrt(vecX * vecX + vecZ * vecZ);
		if (distHoriz < 0) distHoriz = 1;

		double distVert = vecY;

		double factor = distVert / distHoriz;

		/**
		 * 0 = even
		 * - = down
		 * + = up
		 *
		 * use 0.3 has threshold for digging up or down?
		 */

		dbg("factor: " + factor);
    	
    	/*if (rand.nextBoolean()) {
    		//scanX = entity.posX;
        	scanZ = entity.posZ + 0;
    	} else {
    		scanX = entity.posX + 0;
        	//scanZ = entity.posZ;
    	}*/

		boolean newWay = true;


		if (newWay) {

			listPillarToMine.clear();

			//account for being directly above or directly below target
			if (distHoriz <= 1) {
				scanX = entPosX;
				scanZ = entPosZ;
			}

			//i think the y is the block under feet, not where feet occupy
			//actually, must be where feet occupy...
			BlockPos posFrontFeet = new BlockPos(MathHelper.floor(scanX), MathHelper.floor(entity.getEntityBoundingBox().minY), MathHelper.floor(scanZ));


			BlockPos posFeetCheck = new BlockPos(MathHelper.floor(entPosX), MathHelper.floor(entity.getEntityBoundingBox().minY), MathHelper.floor(entPosZ));

			/**
			 * when digging up, or down, need to make sure theres space above to jump up to next pillar
			 * - just up?
			 */

			if (factor <= -0.3F) {

				//down
				dbg("Digging Down");
				listPillarToMine.add(posFrontFeet.up(1));
				listPillarToMine.add(posFrontFeet);
				listPillarToMine.add(posFrontFeet.down(1));


			} else if (factor >= 0.1F) {

				if (!entity.world.isAirBlock(posFeetCheck.up(2))) {
					dbg("Detected block above head, dig it out");
					listPillarToMine.add(posFeetCheck.up(2));
				} else {
					//up
					dbg("Digging Up");
					listPillarToMine.add(posFrontFeet.up(1));
					listPillarToMine.add(posFrontFeet.up(2));
					listPillarToMine.add(posFrontFeet.up(3));
				}



			} else {

				//strait
				dbg("Digging Strait");
				listPillarToMine.add(posFrontFeet.up(1));
				listPillarToMine.add(posFrontFeet);
			}

			boolean fail = false;
			boolean oneMinable = false;

			for (BlockPos pos : listPillarToMine) {
				IBlockState state = entity.world.getBlockState(pos);
				dbg("set: " + pos + " - " + state.getBlock());
			}

			for (BlockPos pos : listPillarToMine) {
				//allow for air for now
				if (!entity.world.isAirBlock(pos) && UtilMining.canMineBlock(entity.world, pos, entity.world.getBlockState(pos).getBlock())) {
					oneMinable = true;
					break;
				}
			}

			if (!oneMinable) {
				dbg("All air blocks or unmineable, cancelling");
				listPillarToMine.clear();
				return false;
			}

			setMiningBlock(entity.world.getBlockState(listPillarToMine.getFirst()), new BlockCoord(listPillarToMine.getFirst()));

			return true;
		} else {
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

    }

    public void setMiningBlock(IBlockState state, BlockCoord pos) {
		dbg("setMiningBlock: " + pos + (state != null ? " - " + state.getBlock() : ""));
		this.posCurMining = pos;
		this.stateCurMining = state;
	}
    
    public void tickMineBlock() {
    	if (posCurMining == null) return;

		IBlockState state = entity.world.getBlockState(posCurMining.toBlockPos());
		Block block = state.getBlock();

		while (entity.world.isAirBlock(posCurMining.toBlockPos()) || !UtilMining.canMineBlock(entity.world, posCurMining.toBlockPos(), entity.world.getBlockState(posCurMining.toBlockPos()).getBlock())) {
			dbg("Detected air or unmineable block, moving to next block in list, cur size: " + listPillarToMine.size());
			if (listPillarToMine.size() > 1) {
				listPillarToMine.removeFirst();
				BlockPos pos = listPillarToMine.getFirst();
				setMiningBlock(entity.world.getBlockState(pos), new BlockCoord(pos));
				//return;
			} else {
				resetTask();
				return;
			}

			state = entity.world.getBlockState(posCurMining.toBlockPos());
			block = state.getBlock();
		}
    	
    	//force stop mining if pushed away, or if block changed
    	if (stateCurMining != state || entity.getDistance(posCurMining.posX, posCurMining.posY, posCurMining.posZ) > 6) {
			dbg("too far or block changed state");
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
                Block.spawnAsEntity(entity.world, posCurMining.toBlockPos(), new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
				entity.world.setBlockToAir(posCurMining.toBlockPos());
            }

            if (listPillarToMine.size() > 1) {
				listPillarToMine.removeFirst();
            	BlockPos pos = listPillarToMine.getFirst();
				setMiningBlock(entity.world.getBlockState(pos), new BlockCoord(pos));
			} else {
            	listPillarToMine.clear();
				setMiningBlock(null, null);
			}


            curBlockDamage = 0;
    		
    	} else {
    		//entity.world.destroyBlockInWorldPartially(entity.getEntityId(), posCurMining.posX, posCurMining.posY, posCurMining.posZ, (int)(curBlockDamage * 10D));
    		entity.world.sendBlockBreakProgress(entity.getEntityId(), posCurMining.toBlockPos(), (int)(curBlockDamage * 10D));
    	}
    }

	@Override
	public boolean shouldBeRemoved() {
		boolean forInvasion = entity.getEntityData().getBoolean(dataUseInvasionRules);

		if (forInvasion) {
			//once its day, disable forever
			if (this.entity.world.isDaytime()) {
				CULog.dbg("removing digging from " + this.entity.getName());
				return true;
				//taskActive = false;
			}
		}

		return false;
	}

	public void dbg(String str) {
    	if (debug) {
			System.out.println(str);
		}
	}
}
