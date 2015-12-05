package CoroPets.ai.tasks;

import java.util.UUID;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowOwner extends EntityAIBase
{
    private EntityLiving thePet;
    //private EntityLivingBase theOwner;
    //public String ownerName;
    public UUID ownerUUID;
    World theWorld;
    private double moveSpeedAdj;
    private PathNavigate petPathfinder;
    private int followTimer;
    float maxDist;
    float minDist;
    private boolean field_75344_i;

    public EntityAIFollowOwner(EntityLiving par1EntityTameable, UUID parOwner, double par2, float par4, float par5)
    {
        this.thePet = par1EntityTameable;
        ownerUUID = parOwner;
        this.theWorld = par1EntityTameable.worldObj;
        this.moveSpeedAdj = par2;
        this.petPathfinder = par1EntityTameable.getNavigator();
        this.minDist = par4;
        this.maxDist = par5;
        this.setMutexBits(3);
    }
    
    //might require caching to be more efficient
    public EntityLivingBase getOwner() {
    	return thePet.worldObj.func_152378_a(ownerUUID);
    	//return thePet.worldObj.getPlayerEntityByName(ownerName);
    }
    
    //method for future order handling
    public boolean canMove() {
    	return true;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityLivingBase entitylivingbase = getOwner();

        if (entitylivingbase == null)
        {
            return false;
        }
        else if (!canMove())
        {
            return false;
        }
        else if (this.thePet.getDistanceSqToEntity(entitylivingbase) < (double)(this.minDist * this.minDist))
        {
            return false;
        }
        else
        {
            //this.theOwner = entitylivingbase;
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
    	EntityLivingBase entitylivingbase = getOwner();
    	if (entitylivingbase == null) return false;
        return !this.petPathfinder.noPath() && this.thePet.getDistanceSqToEntity(entitylivingbase) > (double)(this.maxDist * this.maxDist) && canMove();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.followTimer = 0;
        this.field_75344_i = this.thePet.getNavigator().getAvoidsWater();
        this.thePet.getNavigator().setAvoidsWater(false);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        //this.theOwner = null;
        this.petPathfinder.clearPathEntity();
        this.thePet.getNavigator().setAvoidsWater(this.field_75344_i);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
    	EntityLivingBase entitylivingbase = getOwner();

        if (entitylivingbase == null)
        {
            return;
        }
        
        this.thePet.getLookHelper().setLookPositionWithEntity(entitylivingbase, 10.0F, (float)this.thePet.getVerticalFaceSpeed());

        if (canMove())
        {
            if (--this.followTimer <= 0)
            {
                this.followTimer = 10;

                if (!this.petPathfinder.tryMoveToEntityLiving(entitylivingbase, this.moveSpeedAdj))
                {
                    if (!this.thePet.getLeashed())
                    {
                        if (this.thePet.getDistanceSqToEntity(entitylivingbase) >= 144.0D)
                        {
                            int i = MathHelper.floor_double(entitylivingbase.posX) - 2;
                            int j = MathHelper.floor_double(entitylivingbase.posZ) - 2;
                            int k = MathHelper.floor_double(entitylivingbase.getEntityBoundingBox().minY);

                            for (int l = 0; l <= 4; ++l)
                            {
                                for (int i1 = 0; i1 <= 4; ++i1)
                                {
                                	if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && World.doesBlockHaveSolidTopSurface(this.theWorld, new BlockPos(i + l, k - 1, j + i1)) && !this.theWorld.getBlockState(new BlockPos(i + l, k, j + i1)).getBlock().isNormalCube() && !this.theWorld.getBlockState(new BlockPos(i + l, k + 1, j + i1)).getBlock().isNormalCube())
                                    {
                                        this.thePet.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), this.thePet.rotationYaw, this.thePet.rotationPitch);
                                        this.petPathfinder.clearPathEntity();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
