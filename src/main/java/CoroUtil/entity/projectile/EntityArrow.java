package CoroUtil.entity.projectile;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import CoroUtil.bt.IBTAgent;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorTrail;
import extendedrenderer.particle.entity.EntityRotFX;

public class EntityArrow extends EntityProjectileBase
{
	public int ticksInAir;
	
	@SideOnly(Side.CLIENT)
	public boolean hasDeathTicked;

	public EntityArrow(World world)
	{
		super(world);
	}
	
	public EntityArrow(World par1World, EntityLivingBase par2EntityLivingBase, EntityLivingBase target, double parSpeed)
    {
		super(par1World, par2EntityLivingBase, target, parSpeed);
    }

	public EntityArrow(World world, EntityLivingBase entityliving, double parSpeed)
	{
		super(world, entityliving, parSpeed);
		
		float speed = 0.7F;
		float f = 0.4F;
        this.motionX = (double)(-MathHelper.sin(-this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(-this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionZ = (double)(MathHelper.cos(-this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(-this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionY = (double)(-MathHelper.sin((-this.rotationPitch + this.func_70183_g()) / 180.0F * (float)Math.PI) * f);
        this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, speed, 1.0F);
	}

	public EntityArrow(World world, double d, double d1, double d2)
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
			
			if (ticksInAir > 200) {
				setDead();
			}
        } else {
        	if (particleBehavior == null) {
        		particleBehavior = new ParticleBehaviorTrail(new Vec3(posX, posY, posZ));
        		particleBehavior.sourceEntity = this;
        		//particleBehaviors.rateAlpha = 0.02F;
        		//particleBehaviors.rateBrighten = 0.02F;
        		//particleBehaviors.tickSmokifyTrigger = 20;
        	}
        	tickAnimate();
        }
    }
	
	@Override
	protected float getGravityVelocity() {
		return 0.003F;
	}
	
	@Override
	public MovingObjectPosition tickEntityCollision(Vec3 vec3, Vec3 vec31) {
		MovingObjectPosition movingobjectposition = null;
		
        Entity entity = null;
        List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(0.5D, 1D, 0.5D));
        double d0 = 0.0D;
        EntityLivingBase entityliving = this.getThrower();

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = (Entity)list.get(j);

            if (entity1.canBeCollidedWith() && (entity1 != entityliving && this.ticksInAir >= 4))
            {
                entity = entity1;
                break;
            }
        }

        if (entity != null)
        {
            movingobjectposition = new MovingObjectPosition(entity);
            /*if (movingobjectposition != null) {
            	this.onImpact(movingobjectposition);
            	setDead();
            }*/
        }
        return movingobjectposition;
	}

	@Override
	protected void onImpact(MovingObjectPosition movingobjectposition)
	{
		
		try {
			if (movingobjectposition.entityHit != null)
			{
				if (!worldObj.isRemote)
				{
					
					float damage = 5;
					
					if (movingobjectposition.entityHit instanceof IBTAgent && getThrower() instanceof IBTAgent) {
						if (((IBTAgent) getThrower()).getAIBTAgent().isEnemy(movingobjectposition.entityHit)) {
							/*if (movingobjectposition.entityHit instanceof IEpochEntity && getThrower() instanceof IEpochEntity) {
								CalcCombat.performDamage((IEpochEntity)getThrower(), (IEpochEntity)movingobjectposition.entityHit, 0, new DamageData((IEpochEntity)getThrower(), true, DamageData.TYPE_FIRE, damage));
							} else {*/
								movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), damage);
							//}
							//movingobjectposition.entityHit.setFire(3);
							if (!worldObj.isRemote) {
								setDead();
							} else {
								tickDeath();
							}
						} else {
	
						}
					} else {
						movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), damage);
						if (!worldObj.isRemote) {
							setDead();
						} else {
							tickDeath();
						}
					}
					
					
					
				}
			}
		} catch (Exception ex) {
			//owner entity agent was cleaned up
			ex.printStackTrace();
		}
		
		super.onImpact(movingobjectposition);
		
	}
	
	@Override
	public void setDead() {
		if (worldObj.isRemote) tickDeath();
		super.setDead();
	}
	
	@SideOnly(Side.CLIENT)
	public void tickAnimate() {
		int amount = 10 / (Minecraft.getMinecraft().gameSettings.particleSetting+1);
		
		double speed = 0.15D;
    	double speedInheritFactor = 0.5D;
    	
    	//while (particleBehavior.particles.size() < 200) {
    	for (int i = 0; i < 0; i++) {
	    	//EntityRotFX entityfx = new EntityIconFX(Minecraft.getMinecraft().theWorld, owner.posX + rand.nextDouble(), owner.boundingBox.minY+0.2, owner.posZ + rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed, ParticleRegistry.squareGrey);
    		double randRange = 0.5D;
	    	EntityRotFX entityfx = particleBehavior.spawnNewParticleIconFX(Minecraft.getMinecraft().theWorld, ParticleRegistry.squareGrey, posX - 0.5D*randRange + rand.nextDouble()*randRange, posY - 0.5D*randRange + rand.nextDouble()*randRange, posZ - 0.5D*randRange + rand.nextDouble()*randRange, 0, 0, 0/*(rand.nextDouble() - rand.nextDouble()) * speed, 0.03D(rand.nextDouble() - rand.nextDouble()) * speed, (rand.nextDouble() - rand.nextDouble()) * speed*/);
	    	particleBehavior.initParticle(entityfx);
	    	entityfx.callUpdatePB = false;
			ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
			particleBehavior.particles.add(entityfx);
    	}
	}
	
	@SideOnly(Side.CLIENT)
	public void tickDeath() {
		if (!hasDeathTicked) {
			hasDeathTicked = true;
		}
	}
}
