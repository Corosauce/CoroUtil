package CoroUtil.ai.tasks;

import CoroUtil.ai.IInvasionControlledTask;
import CoroUtil.ai.ITaskInitializer;
import CoroUtil.config.ConfigCoroUtilAdvanced;
import CoroUtil.forge.CULog;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilPath;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * For making use of long distance partial pathing
 * TODO: test it more to make sure it doesnt double up pathfinding work too much
 */
public class EntityAIChaseFromFar extends EntityAIBase implements ITaskInitializer, IInvasionControlledTask
{
    World world;
    protected EntityCreature attacker;
    /** An amount of decrementing ticks that allows the entity to attack once the tick reaches 0. */
    protected int attackTick;
    /** The speed with which the mob will approach the target */
    double speedTowardsTarget;
    /** When true, the mob will continue chasing its target, even if it can't find a path to them right now. */
    boolean longMemory;
    /** The PathEntity of our entity. */
    Path entityPathEntity;
    private int delayCounter;
    private double targetX;
    private double targetY;
    private double targetZ;
    protected final int attackInterval = 20;
    private int failedPathFindingPenalty = 0;

    private boolean disableAtSunrise = true;

    //needed for generic instantiation
    public EntityAIChaseFromFar() {
        this.speedTowardsTarget = 1;
        this.longMemory = false;
        this.setMutexBits(0);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {

        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase == null)
        {
            return false;
        }
        else if (!entitylivingbase.isEntityAlive())
        {
            return false;
        }
        else
        {



            //this.entityPathEntity = this.attacker.getNavigator().getPathToEntityLiving(entitylivingbase);
            if (CoroUtilEntity.canPathfindLongDist(attacker)) {

                boolean debugTPSSpike = false;
                if (debugTPSSpike) {
                    CULog.dbg("EntityAIChaseFromFar shouldExecute trypath: " + attacker.world.getTotalWorldTime());
                }

                CoroUtilPath.tryMoveToEntityLivingLongDist(attacker, entitylivingbase, 1);
                CoroUtilEntity.updateLastTimeLongDistPathfinded(attacker);

                this.entityPathEntity = attacker.getNavigator().getPath();

                if (this.entityPathEntity != null)
                {
                    return true;
                }
                else
                {
                    return this.getAttackReachSqr(entitylivingbase) >= this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
                }
            } else {
                return false;
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase == null)
        {
            return false;
        }
        else if (!entitylivingbase.isEntityAlive())
        {
            return false;
        }
        else if (!this.longMemory)
        {
            return !this.attacker.getNavigator().noPath();
        }
        else if (!this.attacker.isWithinHomeDistanceFromPosition(new BlockPos(entitylivingbase)))
        {
            return false;
        }
        else
        {
            return !(entitylivingbase instanceof EntityPlayer) || !((EntityPlayer)entitylivingbase).isSpectator() && !((EntityPlayer)entitylivingbase).isCreative();
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        this.attacker.getNavigator().setPath(this.entityPathEntity, this.speedTowardsTarget);
        this.delayCounter = 0;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    @Override
    public void resetTask()
    {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase instanceof EntityPlayer && (((EntityPlayer)entitylivingbase).isSpectator() || ((EntityPlayer)entitylivingbase).isCreative()))
        {
            /** DO NOT SET NULL TARGET UNLESS ITS A TARGET TASK, will crash vanilla with this otherwise:
            Caused by: java.lang.NullPointerException
            at net.minecraft.entity.ai.EntityLookHelper.setLookPositionWithEntity(EntityLookHelper.java:31) ~[EntityLookHelper.class:?]
            at net.minecraft.entity.ai.EntityAIAttackMelee.updateTask(EntityAIAttackMelee.java:142) ~[EntityAIAttackMelee.class:?]
            vanilla can get away with this because its own only task this one was based on is the only one that does it in the task list, 2 doing it = crash
            */

            //this.attacker.setAttackTarget((EntityLivingBase)null);
        }

        this.attacker.getNavigator().clearPathEntity();
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    @Override
    public void updateTask()
    {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
        //fix edge case where other code was causing this situation
        if (entitylivingbase == null) {
            resetTask();
            return;
        }
        this.attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
        double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
        --this.delayCounter;

        if ((this.longMemory ||
                this.delayCounter <= 0 && this.attacker.getEntitySenses().canSee(entitylivingbase)) &&
                (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D ||
                        entitylivingbase.getDistanceSq(this.targetX, this.targetY, this.targetZ) >= 1.0D ||
                        this.attacker.getRNG().nextFloat() < 0.05F))
        {
            this.targetX = entitylivingbase.posX;
            this.targetY = entitylivingbase.getEntityBoundingBox().minY;
            this.targetZ = entitylivingbase.posZ;
            this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);

            //this task is designed to path from far, this shouldnt be used
            /*if (d0 > 1024.0D)
            {
                this.delayCounter += 10;
            }
            else if (d0 > 256.0D)
            {
                this.delayCounter += 5;
            }*/

            if (CoroUtilEntity.canPathfindLongDist(attacker)) {
                CoroUtilEntity.updateLastTimeLongDistPathfinded(attacker);
                if (!CoroUtilPath.tryMoveToEntityLivingLongDist(attacker, entitylivingbase, 1))
                {
                    this.delayCounter += 15;
                }
            }


            /*if (!this.attacker.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.speedTowardsTarget))
            {
                this.delayCounter += 15;
            }*/
        }

        //this.attackTick = Math.max(this.attackTick - 1, 0);
        //this.checkAndPerformAttack(entitylivingbase, d0);
    }

    protected double getAttackReachSqr(EntityLivingBase attackTarget)
    {
        return (double)(this.attacker.width * 2.0F * this.attacker.width * 2.0F + attackTarget.width);
    }

    @Override
    public void setEntity(EntityCreature creature) {
        this.attacker = creature;
    }

    @Override
    public boolean shouldBeRemoved() {

        if (disableAtSunrise && ConfigCoroUtilAdvanced.removeInvasionAIWhenInvasionDone) {
            //once its day, disable forever
            if (this.attacker.world.isDaytime()) {
                CULog.dbg("removing long distance pathing from " + this.attacker.getName());
                return true;
                //taskActive = false;
            }
        }

        return false;
    }
}