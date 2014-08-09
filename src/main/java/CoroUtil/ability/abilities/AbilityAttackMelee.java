package CoroUtil.ability.abilities;

import java.util.Random;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntityDamageSource;

import org.lwjgl.opengl.GL11;

import CoroUtil.ability.Ability;
import CoroUtil.entity.render.ModelBipedTagged;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AbilityAttackMelee extends Ability {
	
	//a generic / example class
	
	public EntityLivingBase target;
	
	public AbilityAttackMelee() {
		super();
		this.name = "AttackMelee";
		this.ticksToPerform = 20;
		this.ticksToCooldown = 5;
		
		//max dist of 4
		this.bestDist = 2;
		this.bestDistRange = 4;
	}
	
	@Override
	public void setTarget(Entity parTarget) {
		if (parTarget instanceof EntityLivingBase) {
			target = (EntityLivingBase)parTarget;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void tickRender(ModelBase model) {
		super.tickRender(model);
		
		float amp = 20F;
		
		/*model.bipedLeftArm.rotateAngleX = curTickPerform * amp;
		model.bipedRightArm.rotateAngleX = curTickPerform * amp;*/
		
		GL11.glRotatef(curTickPerform * amp, 1F, 0, 1F);
		
	}

	@Override
	public void tickPerform() {
		
		//System.out.println("isRemote: " + owner.worldObj.isRemote);
		if (owner.worldObj.isRemote) {
			Random rand = new Random();
			//owner.worldObj.spawnParticle("largesmoke", owner.posX + (rand.nextDouble() - 0.5D) * (double)owner.width, owner.posY + rand.nextDouble() * (double)owner.height, owner.posZ + (rand.nextDouble() - 0.5D) * (double)owner.width, 0.0D, 0.0D, 0.0D);
		} else {
			int ticksHitTarg = 15;
			int ticksHitRange = 3;
			
			double speed = 0.8D;
			double hitRange = 3D;
			
			if (target != null && (target.isDead || /*target.getHealth() <= 0 || */(target instanceof EntityLivingBase && ((EntityLivingBase)target).deathTime > 0))) {
				this.setFinishedPerform();
			}
			
			double dist = this.owner.getDistanceToEntity(target);
			
			if (dist < hitRange) {
				if (curTickPerform >= ticksHitTarg - ticksHitRange && curTickPerform <= ticksHitTarg + ticksHitRange) {
					//System.out.println("hit");
					this.target.attackEntityFrom(new EntityDamageSource("mob", owner), 10);
					
					this.setFinishedPerform();
				}
			}
		}
		
		
		super.tickPerform();
	}

}
