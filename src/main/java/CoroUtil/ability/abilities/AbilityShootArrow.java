package CoroUtil.ability.abilities;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import CoroUtil.ability.Ability;
import CoroUtil.bt.IBTAgent;
import CoroUtil.entity.projectile.EntityProjectileBase;
import CoroUtil.inventory.AIInventory;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.EntityRotFX;

public class AbilityShootArrow extends Ability {
	
	public LivingEntity target;
	
	public int projectileType = 0;
	public boolean switchToRangedSlot = true;
	
	public AbilityShootArrow() {
		super();
		this.name = "ShootArrow";
		this.ticksToCharge = 40;
		this.ticksToPerform = 1;
		this.ticksToCooldown = 1;
		
		//5-35
		this.bestDist = 25;
		this.bestDistRange = 30;
	}
	
	@Override
	public void nbtLoad(CompoundNBT nbt) {
		super.nbtLoad(nbt);
		projectileType = nbt.getInt("projectileType");
	}
	
	@Override
	public CompoundNBT nbtSave() {
		CompoundNBT nbt = super.nbtSave();
		nbt.putInt("projectileType", projectileType);
		return nbt;
	}
	
	@Override
	public void setTarget(Entity parTarget) {
		if (parTarget instanceof LivingEntity) {
			target = (LivingEntity)parTarget;
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void tickRender(EntityRenderer parRender) {
		super.tickRender(parRender);
		
		int curTick = curTickPerform;
		//curTick = ticksToPerform/2;
		
		float amp = 1F;
		
		double offset = -Math.PI/4D + -Math.PI/4D/2D;
		double range = Math.PI*2D;
		
		float swap = usageCount % 2 == 0 ? 1 : -1;
		
		/*model.bipedRightArm.rotateAngleX = (float) (offset - (Math.sin(range/ticksToPerform * curTick) * amp));
		model.bipedRightArm.rotateAngleZ = (float) (offset*0.15F - (Math.sin(range/ticksToPerform * curTick) * amp*0.7F*swap));
		
		float reduce = 0.25F;
		
		model.bipedLeftArm.rotateAngleY = (float) (-offset*reduce - (Math.sin(range/ticksToPerform * curTick) * amp*reduce));
		model.bipedLeftArm.rotateAngleX = (float) (offset*0.5F - (Math.sin(range/ticksToPerform * curTick) * amp*reduce));
		model.bipedLeftArm.rotateAngleZ = (float) (offset*reduce - (Math.sin(range/ticksToPerform * curTick) * amp*reduce));*/
		
	}
	
	@Override
	public void tickChargeUp() {
		super.tickChargeUp();
		
		if (owner.world.isRemote) {
			
			Random rand = new Random();
			//double speed = 0.3D;
			//owner.world.addParticle("flame", owner.posX + (rand.nextDouble() - 0.5D) * (double)owner.width, owner.posY + rand.nextDouble() * (double)owner.height, owner.posZ + (rand.nextDouble() - 0.5D) * (double)owner.width, (rand.nextDouble() - 0.5D) * speed, (rand.nextDouble() - 0.5D) * speed, (rand.nextDouble() - 0.5D) * speed);
			//flame hugeexplosion
			
			//debug
			//curTickCharge = 0;

			if (curTickCharge > 0) {
				int amount = 1 + (int)(10D * ((double)curTickCharge / (double)ticksToCharge) / (Minecraft.getInstance().gameSettings.particles+1));
				
				//System.out.println(amount);
				
				for (int i = 0; i < amount; i++)
		        {
		        	double speed = 0.15D;
		        	double speedInheritFactor = 0.5D;
		        	
		        	//EntityRotFX entityfx = new EntityIconFX(Minecraft.getInstance().world, owner.posX + rand.nextDouble(), owner.bounds.minY+0.2, owner.posZ + rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed, ParticleRegistry.squareGrey);
		        	EntityRotFX entityfx = particleBehavior.spawnNewParticleIconFX(Minecraft.getInstance().world, ParticleRegistry.squareGrey, owner.posX + rand.nextDouble(), owner.getBoundingBox().minY+0.8, owner.posZ + rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed);
		        	particleBehavior.initParticle(entityfx);
		        	float f = 0.0F + (rand.nextFloat() * 0.4F);
		        	entityfx.setColor(f, f, f);
		        	entityfx.callUpdatePB = false;
					ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
					particleBehavior.particles.add(entityfx);
					
		        }
			}
			
		} else {
			
			if (switchToRangedSlot) {
				//System.out.println("ranged slot use!");
				if (owner instanceof IBTAgent) {
					((IBTAgent)owner).getAIBTAgent().entInv.setSlotActive(AIInventory.slot_Ranged);
				}
			}
			
			//temp hold in position
			//change!
			owner.motionX *= 0.3F;
			owner.motionY *= 0.3F;
			owner.motionZ *= 0.3F;
		}
	}
	
	@Override
	public void tickCooldown() {
		super.tickCooldown();
	}

	@Override
	public void tickPerform() {
		
		if (target == null) {
			setFinishedPerform();
			return;
		}
		
		//System.out.println("isRemote: " + owner.world.isRemote);
		if (owner.world.isRemote) {
			particleBehavior.particles.clear();
			Random rand = new Random();
			//owner.world.addParticle("largeexplode", owner.posX + (rand.nextDouble() - 0.5D) * (double)owner.width, owner.posY + rand.nextDouble() * (double)owner.height, owner.posZ + (rand.nextDouble() - 0.5D) * (double)owner.width, 0.0D, 0.0D, 0.0D);
		} else {
			
			if (target != null && (target.removed || /*target.getHealth() <= 0 || */(target instanceof LivingEntity && ((LivingEntity)target).deathTime > 0))) {
				//this.setFinishedPerform();
			} else {
				if (!hasAppliedDamage) {
					hasAppliedDamage = true;
					//System.out.println("hit");
					
					if (owner instanceof MobEntity) {
						((MobEntity) owner).faceEntity(target, 180, 180);
					}

					if (target instanceof LivingEntity) {
						EntityProjectileBase prj = null;
						
						if (projectileType == EntityProjectileBase.PRJTYPE_FIREBALL) {
				        	//prj = new EntityArrow(owner.world, owner, (EntityLivingBase)target, 1.7);
				        } else if (projectileType == EntityProjectileBase.PRJTYPE_ICEBALL) {
				        	//block = Block.ice;
				        }
						
						if (prj != null) {
							owner.world.addEntity0(prj);
						}
					}
				}
			}
		}
		
		
		super.tickPerform();
	}

}
