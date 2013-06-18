package CoroAI.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import CoroAI.componentAI.ICoroAI;

public class EntityTropicraftLeafball extends EntityThrowable
{
	public int ticksInAir;

	public EntityTropicraftLeafball(World world)
	{
		super(world);
	}

	public EntityTropicraftLeafball(World world, EntityLiving entityliving)
	{
		super(world, entityliving);
	}

	public EntityTropicraftLeafball(World world, double d, double d1, double d2)
	{
		super(world, d, d1, d2);
	}
	
	@Override
	public void onUpdate()
    {
		super.onUpdate();
		
		if (!this.worldObj.isRemote)
        {
			
			ticksInAir++;
			
			MovingObjectPosition movingobjectposition = null;
			
            Entity entity = null;
            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;
            EntityLiving entityliving = this.getThrower();

            for (int j = 0; j < list.size(); ++j)
            {
                Entity entity1 = (Entity)list.get(j);

                if (entity1.canBeCollidedWith() && (entity1 != entityliving || this.ticksInAir >= 5))
                {
                    float f = 0.3F;
                    //AxisAlignedBB axisalignedbb = entity1.boundingBox.expand((double)f, (double)f, (double)f);
                    //MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);

                    //if (movingobjectposition1 != null)
                    //{
                        //double d1 = vec3.distanceTo(movingobjectposition1.hitVec);

                        //if (d1 < d0 || d0 == 0.0D)
                        //{
                            entity = entity1;
                            //d0 = d1;
                        //}
                    //}
                }
            }

            if (entity != null)
            {
                movingobjectposition = new MovingObjectPosition(entity);
                if (movingobjectposition != null) {
                	this.onImpact(movingobjectposition);
                	setDead();
                }
            }
        }
    }

	@Override
	protected void onImpact(MovingObjectPosition movingobjectposition)
	{
		if (movingobjectposition.entityHit != null)
		{
			if (!worldObj.isRemote)
			{
				byte byte0 = 2;
				if (movingobjectposition.entityHit instanceof c_EnhAI && getThrower() instanceof c_EnhAI) {
					if (((c_EnhAI) getThrower()).dipl_team != ((c_EnhAI) movingobjectposition.entityHit).dipl_team) {
						movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), byte0);
					} else {

					}
				} else if (movingobjectposition.entityHit instanceof ICoroAI && getThrower() instanceof ICoroAI) {
					if (((ICoroAI) getThrower()).getAIAgent().dipl_team != ((ICoroAI) movingobjectposition.entityHit).getAIAgent().dipl_team) {
						movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), byte0);
					} else {

					}
				} else {
					movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), byte0);
				}

				/*if (movingobjectposition.entityHit instanceof EntityBlaze)
            {
                byte0 = 3;
            }*/
            /*     if (movingobjectposition.entityHit instanceof EntityKoaMember && thrower instanceof EntityKoaMember) {
    			if (((EntityKoaMember) thrower).dipl_team != ((EntityKoaMember) movingobjectposition.entityHit).dipl_team) {
    				movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower), byte0);
    			} else {

    			}
    		}
            else if (!(movingobjectposition.entityHit instanceof EntityKoaMemberNew)) { 
            	if (!movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower), byte0));
            	if (thrower instanceof EntityPlayer) {
            		int what = 0;
            	}
            } else if (movingobjectposition.entityHit instanceof EntityKoaMemberNew && thrower instanceof EntityKoaMemberNew) {
    			if (((EntityKoaMemberNew) thrower).dipl_team != ((EntityKoaMemberNew) movingobjectposition.entityHit).dipl_team) {
    				movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower), byte0);
    			} else {

    			}
    		}
        } 
        for (int i = 0; i < 30; i++)
        {
            //worldObj.spawnParticle("snowballpoof", posX, posY, posZ, 0.0D, 0.0D, 0.0D);
        	double speed = 0.01D;
        	EntityTexFX var31 = new EntityTexFX(worldObj, posX, posY, posZ, rand.nextGaussian()*rand.nextGaussian()*speed, rand.nextGaussian()*speed, rand.nextGaussian()*rand.nextGaussian()*speed, (rand.nextInt(80)/10), 0, mod_EntMover.effLeafID);
            var31.setGravity(0.3F);
            Random rand = new Random();
            var31.rotationYaw = rand.nextInt(360);
            mod_ExtendedRenderer.rotEffRenderer.addEffect(var31);
        }
             */

				setDead();
			}
		}
	}
}
