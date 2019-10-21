package CoroUtil.ai.tasks;

import CoroUtil.ai.IInvasionControlledTask;
import CoroUtil.ai.ITaskInitializer;
import CoroUtil.block.BlockRepairingBlock;
import CoroUtil.config.ConfigCoroUtilAdvanced;
import CoroUtil.forge.CULog;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilPath;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Hastily converted code from old hostile worlds, seems to work well enough to start
 */
public class EntityAIHoist extends Goal implements ITaskInitializer, IInvasionControlledTask
{
    protected CreatureEntity entity;
    /** The speed with which the mob will approach the target */
    double speedTowardsTarget;

    public int tryHoist = 0;
    public boolean stackMode = false;
    public int noMoveTicks = 0;

    private Vec3d posLastTracked = null;

    private boolean disableAtSunrise = true;

    //needed for generic instantiation
    public EntityAIHoist() {
        this.speedTowardsTarget = 1;
        this.setMutexBits(0);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {
        return true;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        return true;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {

    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    @Override
    public void resetTask()
    {

    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    @Override
    public void updateTask()
    {
        //CULog.dbg("running hoist");
        //if (true) return;

        LivingEntity target = this.entity.getAttackTarget();

        if (entity.onGround) {
            entity.entityCollisionReduction = 0.4F;
        } else {
            entity.entityCollisionReduction = 1F;
        }

        entity.fallDistance = 0;

        /*if (!stackMode) {
            double speed = Math.sqrt(entity.motionX * entity.motionX*//* + vecY * vecY*//* + entity.motionZ * entity.motionZ);
            //System.out.println(speed);
            if (speed < 0.05D) {
                noMoveTicks++;
            } else {
                noMoveTicks = 0;
            }
        } else {
            //noMoveTicks = 0;
        }*/

        if (posLastTracked == null) {
            posLastTracked = entity.getPositionVector();
        } else {
            if (posLastTracked.distanceTo(entity.getPositionVector()) < 2) {
                noMoveTicks++;
            } else {
                posLastTracked = entity.getPositionVector();
                noMoveTicks = 0;
            }
        }

        //DEBUG
        //stackMode = true;

        //System.out.println(speed);

        double factor = -1;

        if (!entity.world.isRemote) {
            if (target != null) {
                double xDiff = target.posX - entity.posX;
                double zDiff = target.posZ - entity.posZ;
                double distHoriz = MathHelper.sqrt(xDiff * xDiff + zDiff * zDiff);
                if (distHoriz < 0) distHoriz = 1;

                double distVert = target.posY - entity.posY;

                factor = distVert / distHoriz;
            }
        }



        if (!stackMode) {
            if (target != null && entity.getNavigator().noPath() && !entity.isInWater() && (noMoveTicks > 60 || (factor != -1 && factor > 5))) {
                stackMode = true;
                noMoveTicks = 0;
            }
        } else {
            if (target == null) {
                stackMode = false;
            }
        }

		/*double rangeBox = 0.8D;
		double rangeShiftToStack = 0.5D;
		double rangeShiftToTarget = 0.2D;*/

        double rangeBox = 1.5D;
        double lungeSpeed = 0.4D;
        double rangeShiftBase = 0.4D;
        double rangeShiftAdjToStack = 0.3D;
        double rangeShiftAdjToTarget = 0.1D;
        double rangeShiftAdjFromTarget = 0.3D;
        double rangeNeededToShiftToTarget = 1.4D;

        /* || isNearWall(getEntityBoundingBox()) || isOnLadder()*/
        if (!entity.world.isRemote && stackMode) {
            List<LivingEntity> var2 = entity.world.getEntitiesWithinAABB(LivingEntity.class, entity.getEntityBoundingBox().expand(rangeBox, 2D, rangeBox));

            Random rand = new Random();
            Entity ent = null;

            if (var2.size() > 1) {
                for (int i = 0; i < var2.size(); i++) {
                    if (var2.get(i) != entity && var2.get(i) instanceof ZombieEntity &&
                            entity.getEntityId() > var2.get(i).getEntityId() &&
                            entity.getEntityBoundingBox().minY+1.5D > var2.get(i).getEntityBoundingBox().minY &&
                            entity.getEntityBoundingBox().minY < var2.get(i).getEntityBoundingBox().maxY) {
                        ent = var2.get(i);
                        break;
                    }
                }
            }
            //entity.getEntityBoundingBox().minY+0.001D > var2.get(i).getEntityBoundingBox().minY && entity.getEntityBoundingBox().minY < var2.get(i).getEntityBoundingBox().maxY
            if (tryHoist > 0) {
                if (target.getDistanceToEntity(entity) < 3F || (tryHoist == 1 && target.getEntityBoundingBox().minY < entity.getEntityBoundingBox().minY - 1)) {
                    double vecX = target.posX - entity.posX;
                    double vecY = target.posY - entity.posY;
                    double vecZ = target.posZ - entity.posZ;

                    double var9 = (double) MathHelper.sqrt(vecX * vecX/* + vecY * vecY*/ + vecZ * vecZ);

                    double shiftSpeed = lungeSpeed;
                    entity.motionX = vecX / var9 * shiftSpeed;
                    entity.motionY = 0.5;
                    entity.motionZ = vecZ / var9 * shiftSpeed;
                    //System.out.println("hoissst!");
                    tryHoist = 0;
                    stackMode = false;

                }
                tryHoist--;
            }

            //if (onGround) tryHoist = 0;

            if (ent != null) {
                if (target.getEntityBoundingBox().minY > entity.getEntityBoundingBox().minY + 2/* && entity.getNavigator().noPath()*/) {
                    tryHoist = 40;
                } else if (target.getEntityBoundingBox().minY < entity.getEntityBoundingBox().minY - 1 || !isNearWall(entity.getEntityBoundingBox())) {

                    //tryHoist = 0;
                }



                if (tryHoist > 0) {

                    //break through leafs
                    if (!entity.world.isRemote) {
                        double tryX = entity.posX-0.8D+rand.nextFloat();
                        double tryY = entity.posY+0.5D+(rand.nextFloat() * 2D);
                        double tryZ = entity.posZ-0.8D+rand.nextFloat();
                        BlockPos pos = new BlockPos(tryX, tryY, tryZ);
                        BlockState state = entity.world.getBlockState(pos);
                        //Block id = entity.world.getBlock((int)(tryX), (int)(tryY), (int)(tryZ));
                        if (!CoroUtilBlock.isAir(state.getBlock()) && (state.getMaterial() == Material.LEAVES || state.getMaterial() == Material.PLANTS) && !(state.getBlock() instanceof BlockRepairingBlock)) {
                            //System.out.println("remove leafs!");
                            entity.world.setBlockToAir(pos);
                        }
                    }

                    double shiftSpeed = rangeShiftBase;
					/*entity.motionX += Math.sin(ent.posX - posX) * shiftSpeed;
					entity.motionZ -= Math.sin(ent.posZ - posZ) * shiftSpeed;*/
                    double var9;
                    double vecX = ent.posX - entity.posX;
                    double vecY = ent.posY - entity.posY;
                    double vecZ = ent.posZ - entity.posZ;

                    double dist = (double)MathHelper.sqrt(vecX * vecX/* + vecY * vecY*/ + vecZ * vecZ);
                    if (dist > 0.1D) {
                        entity.motionX = vecX / dist * shiftSpeed * rangeShiftAdjToStack;
                        //entity.motionY = vecY / var9 * shiftSpeed;
                        entity.motionZ = vecZ / dist * shiftSpeed * rangeShiftAdjToStack;
                    }

                    //if (target != null) {
                    vecX = target.posX - entity.posX;
                    vecY = target.posY - entity.posY;
                    vecZ = target.posZ - entity.posZ;
                    //}


                    var9 = (double)MathHelper.sqrt(vecX * vecX/* + vecY * vecY*/ + vecZ * vecZ);
                    if (dist+shiftSpeed < rangeNeededToShiftToTarget) {
                        BlockPos pos1 = new BlockPos(entity.posX+0.0D+(vecX/var9*1.5F), entity.posY, entity.posZ+0.0D+(vecZ/var9*1.5F));
                        BlockPos pos2 = new BlockPos(entity.posX+0.5D, 0, entity.posZ+0.5D);
                        int height = entity.world.getHeight(pos1).getY();
                        int height2 = entity.world.getHeight(pos2).getY();
                        if (height <= entity.posY || (height2 > target.posY + 1)) {
                            entity.motionX = vecX / var9 * shiftSpeed * rangeShiftAdjToTarget;
                            //entity.motionY = vecY / var9 * shiftSpeed;
                            entity.motionZ = vecZ / var9 * shiftSpeed * rangeShiftAdjToTarget;
                        } else {
                            entity.motionX = -vecX / var9 * shiftSpeed * /*rand.nextFloat() * */rangeShiftAdjFromTarget;
                            //entity.motionY = vecY / var9 * shiftSpeed;
                            entity.motionZ = -vecZ / var9 * shiftSpeed * /*rand.nextFloat() * */rangeShiftAdjFromTarget;
                        }
                    }



                    float wat = 0.15F;// + rand.nextFloat() * 0.05F;

                    //if (wat > 0.2F) wat = 0.2F;

                    if (entity.motionY < -0.0) entity.motionY = -0.0F;

                    entity.motionY += wat;//+= Math.min(0.3F, Math.max(0F, var2.size()-1) * (/*rand.nextFloat() * */0.025F));

                    if (entity.motionY > 0.15F) entity.motionY = 0.15F;
                    if (ent.onGround) {
                        //ent.motionX = -vecX / var9 * shiftSpeed;
                        //ent.motionZ = -vecZ / var9 * shiftSpeed;
                        //ent.moveEntity(ent.motionX * 15D, 0D, ent.motionZ * 15D);
                    } else {


                    }
                }
            } else {
                //stackMode = false;
            }

        } else {

        }

        if (!entity.world.isRemote) {
            if (entity.isOnLadder()) {
                //entity.motionY = -0.2F;
            }
        }

    }

    protected double getAttackReachSqr(LivingEntity attackTarget)
    {
        return (double)(this.entity.width * 2.0F * this.entity.width * 2.0F + attackTarget.width);
    }

    @Override
    public void setEntity(CreatureEntity creature) {
        this.entity = creature;
    }

    @Override
    public boolean shouldBeRemoved() {

        if (disableAtSunrise && ConfigCoroUtilAdvanced.removeInvasionAIWhenInvasionDone) {
            //once its day, disable forever
            if (this.entity.world.isDaytime()) {
                CULog.dbg("removing hoisting from " + this.entity.getName());
                return true;
            }
        }

        return false;
    }

    public boolean isNearWall(AxisAlignedBB par1AxisAlignedBB)
    {
        int var3 = MathHelper.floor(par1AxisAlignedBB.minX);
        int var4 = MathHelper.floor(par1AxisAlignedBB.maxX + 1.0D);
        int var5 = MathHelper.floor(par1AxisAlignedBB.minY);
        int var6 = MathHelper.floor(par1AxisAlignedBB.maxY + 1.0D);
        int var7 = MathHelper.floor(par1AxisAlignedBB.minZ);
        int var8 = MathHelper.floor(par1AxisAlignedBB.maxZ + 1.0D);

        for (int var9 = var3-1; var9 < var4+1; ++var9)
        {
            for (int var10 = var5; var10 < var6+1; ++var10)
            {
                for (int var11 = var7-1; var11 < var8+1; ++var11)
                {
                    Block var12 = entity.world.getBlockState(new BlockPos(var9, var10, var11)).getBlock();

                    if (!CoroUtilBlock.isAir(var12)/* && var12.blockMaterial == par2Material*/)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}