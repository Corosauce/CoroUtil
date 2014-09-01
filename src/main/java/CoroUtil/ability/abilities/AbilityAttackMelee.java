package CoroUtil.ability.abilities;

import java.util.Random;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntityDamageSource;

import org.lwjgl.opengl.GL11;

import CoroUtil.ability.Ability;
import CoroUtil.bt.IBTAgent;
import CoroUtil.entity.render.ModelBipedTagged;
import CoroUtil.inventory.AIInventory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AbilityAttackMelee extends Ability {
	
	//a generic / example class
	
	public EntityLivingBase target;
	public boolean switchToMeleeSlot = true;
	
	public AbilityAttackMelee() {
		super();
		this.name = "AttackMelee";
		this.ticksToPerform = 20;
		this.ticksToCooldown = 20;
		
		//max dist of 3
		this.bestDist = 1;
		this.bestDistRange = 3;
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
		
		float amp = 20F;
		
		/*model.bipedLeftArm.rotateAngleX = curTickPerform * amp;
		model.bipedRightArm.rotateAngleX = curTickPerform * amp;*/
		
		//GL11.glRotatef(curTickPerform * amp, 1F, 0, 1F);
		
	}

	@Override
	public void tickPerform() {
		
		if (target == null) {
			setFinishedPerform();
			return;
		}
		
		//System.out.println("isRemote: " + owner.worldObj.isRemote);
		if (owner.worldObj.isRemote) {
			Random rand = new Random();
			//owner.worldObj.spawnParticle("largesmoke", owner.posX + (rand.nextDouble() - 0.5D) * (double)owner.width, owner.posY + rand.nextDouble() * (double)owner.height, owner.posZ + (rand.nextDouble() - 0.5D) * (double)owner.width, 0.0D, 0.0D, 0.0D);
		} else {
			
			if (switchToMeleeSlot) {
				//System.out.println("melee slot use!");
				if (owner instanceof IBTAgent) {
					((IBTAgent)owner).getAIBTAgent().entInv.setSlotActive(AIInventory.slot_Melee);
				}
			}
			
			int ticksHitTarg = 8;
			int ticksHitRange = 3;
			
			double speed = 0.8D;
			double hitRange = 1.5D;
			
			if (target != null && (target.isDead || /*target.getHealth() <= 0 || */(target instanceof EntityLivingBase && ((EntityLivingBase)target).deathTime > 0))) {
				this.setFinishedPerform();
			}
			
			if (curTickPerform == 1) {
				this.owner.swingItem();
			}
			
			if (target != null) {
				double dist = this.owner.getDistanceToEntity(target);
				
				if (dist <= hitRange) {
					
					if (curTickPerform >= ticksHitTarg - ticksHitRange && curTickPerform <= ticksHitTarg + ticksHitRange) {
						//System.out.println("hit");
						this.target.attackEntityFrom(new EntityDamageSource("mob", owner), 2);
						this.setFinishedPerform();
					}
				}
			}
		}
		
		
		super.tickPerform();
	}

}
