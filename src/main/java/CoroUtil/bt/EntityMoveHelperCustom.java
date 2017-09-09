package CoroUtil.bt;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.MathHelper;

public class EntityMoveHelperCustom
{
    /** The EntityLiving that is being moved */
    private EntityLivingBase entity;
    private double posX;
    private double posY;
    private double posZ;

    /** The speed at which the entity should move */
    private double speed;
    private boolean update;
    
    public boolean canSwimInWater; //used for navigating under the water without strait up float forcing (float code is elsewhere usually)
    public boolean canFly; //used for navigating flying paths

    public EntityMoveHelperCustom(EntityLivingBase par1EntityLiving)
    {
        this.entity = par1EntityLiving;
        this.posX = par1EntityLiving.posX;
        this.posY = par1EntityLiving.posY;
        this.posZ = par1EntityLiving.posZ;
    }

    public boolean isUpdating()
    {
        return this.update;
    }

    public double getSpeed()
    {
        return this.speed;
    }

    /**
     * Sets the speed and location to move to
     */
    public void setMoveTo(double par1, double par3, double par5, double par7)
    {
        this.posX = par1;
        this.posY = par3;
        this.posZ = par5;
        this.speed = par7;
        this.update = true;
    }

    public void onUpdateMoveHelper()
    {
    	
    	//System.out.println("custom mover updating");
    	
    	if (entity instanceof EntityLiving) {
    		((EntityLiving)this.entity).setMoveForward(0.0F);
    	} else {
    		System.out.println("EntityMoveHelperCustom being used on non EntityLiving entity, needs code patch");
    	}
        

        if (this.update)
        {
            this.update = false;
            int i = MathHelper.floor(this.entity.getEntityBoundingBox().minY + 0.5D);
            double d0 = this.posX - this.entity.posX;
            double d1 = this.posZ - this.entity.posZ;
            double d2 = this.posY - (double)i;
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;

            if (d3 >= 2.500000277905201E-7D)
            {
                float f = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
                this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f, 90.0F);
                
                float extraSpeed = 1F;
                
                if (entity.isInWater()) {
                	extraSpeed = 4F;
                }
                
                this.entity.setAIMoveSpeed((float)(extraSpeed * this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
                
                if (canFly || canSwimInWater) {
                	this.entity.jumpMovementFactor = (float)(0.5F * this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                }
                
                /*if (d2 > 0.0D) {
	                if (entity.onGround) {
	    				((EntityLiving)this.entity).getJumpHelper().setJumping();
	    			}
                }*/

                if (d2 > 0.0D/* && d0 * d0 + d1 * d1 < 1.0D*/)
                {
                	if (entity instanceof EntityLiving) {
                		if (canFly || canSwimInWater) {
                			//System.out.println("fly up test");
                			
                			
                			
                			/*if (entity.isInWater()) {
                				extraY = 10F;
                			}*/
                			
                			entity.motionY = 1F * this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
                		} else {
                			((EntityLiving)this.entity).getJumpHelper().setJumping();
                		}
                	} else {
                		System.out.println("EntityMoveHelperCustom being used on non EntityLiving entity, needs code patch");
                	}
                } else {
                	if (canFly || canSwimInWater) {
                		if (d2 < 0.0D/* && d0 * d0 + d1 * d1 < 1.0D*/) {
                			//System.out.println("fly down test");
                			//entity.motionY = -0.5F * this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
                		}
                	}
                }
                
                if (d2 > 0.0D) {
                	if (entity.isInWater() || entity.onGround) {
                		entity.motionY = 1F * this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
                	}
                }
            }
        }
    }

    /**
     * Limits the given angle to a upper and lower limit.
     */
    private float limitAngle(float par1, float par2, float par3)
    {
        float f3 = MathHelper.wrapDegrees(par2 - par1);

        if (f3 > par3)
        {
            f3 = par3;
        }

        if (f3 < -par3)
        {
            f3 = -par3;
        }

        return par1 + f3;
    }
}
