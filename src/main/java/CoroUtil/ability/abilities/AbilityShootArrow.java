package CoroUtil.ability.abilities;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import CoroUtil.ability.Ability;
import CoroUtil.bt.IBTAgent;
import CoroUtil.entity.projectile.EntityArrow;
import CoroUtil.entity.projectile.EntityProjectileBase;
import CoroUtil.inventory.AIInventory;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.EntityRotFX;

public class AbilityShootArrow extends Ability {
	
	public EntityLivingBase target;
	
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
	public void nbtLoad(NBTTagCompound nbt) {
		super.nbtLoad(nbt);
		projectileType = nbt.getInteger("projectileType");
	}
	
	@Override
	public NBTTagCompound nbtSave() {
		NBTTagCompound nbt = super.nbtSave();
		nbt.setInteger("projectileType", projectileType);
		return nbt;
	}
	
	@Override
	public void setTarget(Entity parTarget) {
		if (parTarget instanceof EntityLivingBase) {
			target = (EntityLivingBase)parTarget;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void tickRender(Render parRender) {
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
		
		if (owner.worldObj.isRemote) {
			
			Random rand = new Random();
			//double speed = 0.3D;
			//owner.worldObj.spawnParticle("flame", owner.posX + (rand.nextDouble() - 0.5D) * (double)owner.width, owner.posY + rand.nextDouble() * (double)owner.height, owner.posZ + (rand.nextDouble() - 0.5D) * (double)owner.width, (rand.nextDouble() - 0.5D) * speed, (rand.nextDouble() - 0.5D) * speed, (rand.nextDouble() - 0.5D) * speed);
			//flame hugeexplosion
			
			//debug
			//curTickCharge = 0;

			if (curTickCharge > 0) {
				int amount = 1 + (int)(10D * ((double)curTickCharge / (double)ticksToCharge) / (Minecraft.getMinecraft().gameSettings.particleSetting+1));
				
				//System.out.println(amount);
				
				for (int i = 0; i < amount; i++)
		        {
		        	double speed = 0.15D;
		        	double speedInheritFactor = 0.5D;
		        	
		        	//EntityRotFX entityfx = new EntityIconFX(Minecraft.getMinecraft().theWorld, owner.posX + rand.nextDouble(), owner.boundingBox.minY+0.2, owner.posZ + rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed, ParticleRegistry.squareGrey);
		        	EntityRotFX entityfx = particleBehavior.spawnNewParticleIconFX(Minecraft.getMinecraft().theWorld, ParticleRegistry.squareGrey, owner.posX + rand.nextDouble(), owner.boundingBox.minY+0.8, owner.posZ + rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed);
		        	particleBehavior.initParticle(entityfx);
		        	float f = 0.0F + (rand.nextFloat() * 0.4F);
		        	entityfx.setRBGColorF(f, f, f);
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
		
		//System.out.println("isRemote: " + owner.worldObj.isRemote);
		if (owner.worldObj.isRemote) {
			particleBehavior.particles.clear();
			Random rand = new Random();
			//owner.worldObj.spawnParticle("largeexplode", owner.posX + (rand.nextDouble() - 0.5D) * (double)owner.width, owner.posY + rand.nextDouble() * (double)owner.height, owner.posZ + (rand.nextDouble() - 0.5D) * (double)owner.width, 0.0D, 0.0D, 0.0D);
		} else {
			
			if (target != null && (target.isDead || /*target.getHealth() <= 0 || */(target instanceof EntityLivingBase && ((EntityLivingBase)target).deathTime > 0))) {
				//this.setFinishedPerform();
			} else {
				if (!hasAppliedDamage) {
					hasAppliedDamage = true;
					//System.out.println("hit");
					
					if (owner instanceof EntityLiving) {
						((EntityLiving) owner).faceEntity(target, 180, 180);
					}

					if (target instanceof EntityLivingBase) {
						EntityProjectileBase prj = null;
						
						if (projectileType == EntityProjectileBase.PRJTYPE_FIREBALL) {
				        	prj = new EntityArrow(owner.worldObj, owner, (EntityLivingBase)target, 1.7);
				        } else if (projectileType == EntityProjectileBase.PRJTYPE_ICEBALL) {
				        	//block = Block.ice;
				        }
						
						if (prj != null) {
							owner.worldObj.spawnEntityInWorld(prj);
						}
					}
				}
			}
		}
		
		
		super.tickPerform();
	}

}
