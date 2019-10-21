package CoroUtil.ai.tasks;

import CoroUtil.ai.IInvasionControlledTask;
import CoroUtil.ai.ITaskInitializer;
import CoroUtil.block.TileEntityRepairingBlock;
import CoroUtil.config.ConfigCoroUtilAdvanced;
import CoroUtil.forge.CULog;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilPath;
import CoroUtil.util.UtilMining;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;

public class TaskExplodeTowardsTarget extends Goal implements ITaskInitializer, IInvasionControlledTask
{
    private CreatureEntity entity = null;
    private LivingEntity targetLastTracked = null;
    private int noMoveTicks = 0;

    private Vec3d posLastTracked = null;

    public boolean debug = true;

	/**
	 * Fields used when task is added via invasions, but not via hwmonsters, or do we want that too?
	 */
	public static String dataUseInvasionRules = "HW_Inv_UseInvasionRules";

	//TODO: might cause tps spike if everyone with task runs at same time, how to fix given task running not always a guarantee?
	public long lastTimePathChecked = 0;
	public int pathableCheckCountMax = 60;

	public int failedPathCount = 0;
	public int failedPathMax = 5;

	//needed for generic instantiation
    public TaskExplodeTowardsTarget()
    {
        this.setMutexBits(3);
    }
    
    @Override
    public void setEntity(CreatureEntity creature) {
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
    	int noMoveThreshold = 60;
    	/*if (entity.motionX < movementThreshold && entity.motionX > -movementThreshold &&
    			entity.motionZ < movementThreshold && entity.motionZ > -movementThreshold) {
    		
    		noMoveTicks++;
    		
    	} else {
    		noMoveTicks = 0;
			failedPathCount = 0;
    	}*/

    	if (posLastTracked == null) {
    		posLastTracked = entity.getPositionVector();
		} else {
    		if (posLastTracked.distanceTo(entity.getPositionVector()) < 2) {
				noMoveTicks++;
			} else {
				posLastTracked = entity.getPositionVector();
				noMoveTicks = 0;
				failedPathCount = 0;
			}
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
					noMoveTicks = 0;
					failedPathCount = 0;
					return false;
				}
    		} else {
    			targetLastTracked = entity.getAttackTarget();
    		}

    		//if (!entity.getNavigator().noPath()) System.out.println("path size: " + entity.getNavigator().getPath().getCurrentPathLength());
    		if ((isClosestPathable() && noMoveTicks > noMoveThreshold) && entity.getAttackTarget() != null) {
    			return true;
    		} else {
    			//clause for if stuck trying to path
    		}
    	}
    	
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
	@Override
    public void startExecuting()
    {
		explode();
		resetTask();
    	//System.out.println("start!");
    }

    /**
     * Resets the task
     */
	@Override
    public void resetTask()
    {

    }

	@Override
	public boolean shouldBeRemoved() {
		boolean forInvasion = entity.getEntityData().getBoolean(dataUseInvasionRules);

		if (forInvasion && ConfigCoroUtilAdvanced.removeInvasionAIWhenInvasionDone) {
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
    	    CULog.dbg(str);
			//System.out.println(str);
		}
	}

	private void explode()
	{
		if (!entity.world.isRemote)
		{
			boolean flag = entity.world.getGameRules().getBoolean("mobGriefing");
			entity.world.createExplosion(entity, entity.posX, entity.posY, entity.posZ, (float)3, flag);
			entity.setDead();
		}
	}

	/**
	 * Figure out if we are as close as possible to target and cant path any better route
	 *
	 * Since we cant be fully sure last time they tried to path and it failed, lets do it ourselves
	 * must be carefull to not overuse, this is expensive
	 * might be best to just be a temp solution?
	 *
	 * only ever return true if it tried and didnt get much path
	 *
	 * @return
	 */
	public boolean isClosestPathable() {
		if (entity.world.getTotalWorldTime() > lastTimePathChecked + pathableCheckCountMax) {
			if (entity.getAttackTarget() == null) {
				failedPathCount = 0;
				return false;
			}
			if (entity.onGround || entity.isInWater()) {

				//new rule to fix being above target but always pathing
				//AIChaseFromFar uses my long distance pathing, which tries really hard to give them a path, even when directly below their target
				//so they keep wandering around randomly given paths never staying still for long, this code is to work around that issue
				boolean explodeAboveInstantly = true;
				if (explodeAboveInstantly) {
					double vecX = entity.getAttackTarget().posX - entity.posX;
					//feet
					double vecY = entity.getAttackTarget().getEntityBoundingBox().minY - entity.getEntityBoundingBox().minY;
					double vecZ = entity.getAttackTarget().posZ - entity.posZ;

					double distHoriz = Math.sqrt(vecX * vecX + vecZ * vecZ);
					if (distHoriz < 0) distHoriz = 1;

					double factor = vecY / distHoriz;

					/**
					 * target is:
					 * 0 = even
					 * - = down
					 * + = up
					 */

					//if 5 blocks above them and within 5 away xz
					if (factor < -5 && distHoriz < 5) {
						//dbg("factor: " + factor + ", " + noMoveTicks);
						//special rule, just force the explode if theyve just barely not moved
						if (noMoveTicks > 15) {
							explode();
							resetTask();
						}
						return true;
					}
				}

				//abort any future cpu use if it actually has a current path
				if (!entity.getNavigator().noPath() || (entity.getNavigator().getPath() != null && entity.getNavigator().getPath().getCurrentPathLength() > 1)) {
					failedPathCount = 0;
					return false;
				}

				lastTimePathChecked = entity.world.getTotalWorldTime();


				//backup path since tryMoveToEntityLivingLongDist will override existing one and we just want to query if theres a path
				//this probably isnt actually needed since we above made sure they didnt already have a good path, but ehhh
				Path lastPath = entity.getNavigator().getPath();

				//Path newPath = this.entity.getNavigator().getPathToEntityLiving(entity.getAttackTarget());
				CoroUtilPath.tryMoveToEntityLivingLongDist(entity, entity.getAttackTarget(), 1);

				//now that tryMoveToEntityLivingLongDist, get the path it maybe set
				Path newPath = entity.getNavigator().getPath();

				//restore old path
				entity.getNavigator().setPath(lastPath, 1);

				//Path path = this.entity.getNavigator().getPathToEntityLiving(entity.getAttackTarget());
				if (newPath == null || newPath.getCurrentPathLength() <= 1) {
					/*if (newPath == null) {
						CULog.dbg("path null");
					} else {
						CULog.dbg("path len: " + newPath.getCurrentPathLength());
					}*/
					failedPathCount++;
					//CULog.dbg("failedPathCount: " + failedPathCount + ", noMoveTicks: " + noMoveTicks);
					if (failedPathCount >= 2) {
						failedPathCount = 0;
						return true;
					}
					//if (newPath != null && newPath.getCurrentPathLength() <= 1) {

					//}
				}
			}
		}
		return false;
	}
}
