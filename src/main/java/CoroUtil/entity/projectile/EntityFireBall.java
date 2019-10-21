package CoroUtil.entity.projectile;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import CoroUtil.bt.IBTAgent;
import CoroUtil.util.Vec3;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorTrail;
import extendedrenderer.particle.entity.EntityRotFX;

public class EntityFireBall extends EntityProjectileBase
{
	public int ticksInAir;
	
	@OnlyIn(Dist.CLIENT)
	public boolean hasDeathTicked;

	public EntityFireBall(World world)
	{
		super(world);
	}
	
	public EntityFireBall(World par1World, LivingEntity par2EntityLivingBase, LivingEntity target, double parSpeed)
    {
		super(par1World, par2EntityLivingBase, target, parSpeed);
    }

	public EntityFireBall(World world, LivingEntity entityliving, double parSpeed, float parYaw, float parPitch)
	{
		super(world, entityliving, parSpeed);
		
		float speed = 0.7F;
		float f = 0.4F;
        this.motionX = (double)(-MathHelper.sin(-parYaw / 180.0F * (float)Math.PI) * MathHelper.cos(-parPitch / 180.0F * (float)Math.PI) * f);
        this.motionZ = (double)(MathHelper.cos(-parYaw / 180.0F * (float)Math.PI) * MathHelper.cos(-parPitch / 180.0F * (float)Math.PI) * f);
        this.motionY = (double)(-MathHelper.sin((-parPitch + this.func_70183_g()) / 180.0F * (float)Math.PI) * f);
        this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, speed, 1.0F);
	}
	
	public EntityFireBall(World world, LivingEntity entityliving, double parSpeed)
	{
		super(world, entityliving, parSpeed);
		
		float speed = 0.7F;
		float f = 0.4F;
        this.motionX = (double)(-MathHelper.sin(-this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(-this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionZ = (double)(MathHelper.cos(-this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(-this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionY = (double)(-MathHelper.sin((-this.rotationPitch + this.func_70183_g()) / 180.0F * (float)Math.PI) * f);
        this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, speed, 1.0F);
	}

	public EntityFireBall(World world, double d, double d1, double d2)
	{
		super(world, d, d1, d2);
	}
	
	@Override
	public void onUpdate()
    {
		super.onUpdate();
		
		noClip = false;
		
		if (!this.world.isRemote)
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
		return 0F;
	}
	
	@Override
	public RayTraceResult tickEntityCollision(Vec3 vec3, Vec3 vec31) {
		RayTraceResult movingobjectposition = null;
		
        Entity entity = null;
        List list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(0.5D, 1D, 0.5D));
        double d0 = 0.0D;
        LivingEntity entityliving = this.getThrower();

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
            movingobjectposition = new RayTraceResult(entity);
            /*if (movingobjectposition != null) {
            	this.onImpact(movingobjectposition);
            	setDead();
            }*/
        }
        return movingobjectposition;
	}

	@Override
	protected void onImpact(RayTraceResult movingobjectposition)
	{
		
		try {
			if (movingobjectposition.entityHit != null)
			{
				if (!world.isRemote)
				{
					
					float damage = 5;
					
					if (movingobjectposition.entityHit instanceof IBTAgent && getThrower() instanceof IBTAgent) {
						if (((IBTAgent) getThrower()).getAIBTAgent().isEnemy(movingobjectposition.entityHit)) {
							/*if (movingobjectposition.entityHit instanceof IEpochEntity && getThrower() instanceof IEpochEntity) {
								CalcCombat.performDamage((IEpochEntity)getThrower(), (IEpochEntity)movingobjectposition.entityHit, 0, new DamageData((IEpochEntity)getThrower(), true, DamageData.TYPE_FIRE, damage));
							} else {*/
								movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), damage);
							//}
							movingobjectposition.entityHit.setFire(3);
							if (!world.isRemote) {
								setDead();
							} else {
								tickDeath();
							}
						} else {
	
						}
					} else {
						movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), damage);
						if (!world.isRemote) {
							setDead();
						} else {
							tickDeath();
						}
					}
					
					
					
				}
			}
		} catch (Exception ex) {
			//owner entity agent was cleaned up
			//ex.printStackTrace();
		}
		
		super.onImpact(movingobjectposition);
	}
	
	@Override
	public void setDead() {
		if (world.isRemote) tickDeath();
		super.setDead();
	}
	
	@OnlyIn(Dist.CLIENT)
	public void tickAnimate() {
		int amount = 10 / (Minecraft.getMinecraft().gameSettings.particleSetting+1);
		
		double speed = 0.15D;
    	double speedInheritFactor = 0.5D;
    	
    	//while (particleBehavior.particles.size() < 200) {
    	for (int i = 0; i < amount; i++) {
	    	//EntityRotFX entityfx = new EntityIconFX(Minecraft.getMinecraft().world, owner.posX + rand.nextDouble(), owner.boundingBox.minY+0.2, owner.posZ + rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed, ParticleRegistry.squareGrey);
    		double randRange = 0.5D;
	    	EntityRotFX entityfx = particleBehavior.spawnNewParticleIconFX(Minecraft.getMinecraft().world, ParticleRegistry.squareGrey, posX - 0.5D*randRange + rand.nextDouble()*randRange, posY - 0.5D*randRange + rand.nextDouble()*randRange, posZ - 0.5D*randRange + rand.nextDouble()*randRange, 0, 0, 0/*(rand.nextDouble() - rand.nextDouble()) * speed, 0.03D(rand.nextDouble() - rand.nextDouble()) * speed, (rand.nextDouble() - rand.nextDouble()) * speed*/);
	    	particleBehavior.initParticle(entityfx);
	    	entityfx.callUpdatePB = false;
			ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
			particleBehavior.particles.add(entityfx);
    	}
	}
	
	@OnlyIn(Dist.CLIENT)
	public void tickDeath() {
		if (!hasDeathTicked) {
			hasDeathTicked = true;
		}
	}
}
