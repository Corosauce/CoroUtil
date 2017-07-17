package CoroUtil.ability;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import CoroUtil.util.Vec3;
import extendedrenderer.particle.behavior.ParticleBehaviorCharge;


public class Ability {

	//Times to send nbt to client:
	//- Initial (full disk sync)
	//- New Ability add (full disk sync)
	//- runtime state update (new tick state)

	//If ability gets interrupted for state, or goes to next tick, send an update, that tells client its on the next stage at the right time

	//New cooldown design intention:
	//Cooldown shouldnt be factored into isActive, setFinishedEntirely should be used as a cooldown starter
	//this design change will let the inner AI system use other skills while this one is still cooling down, better management and less locking
	//atm all abilities use 'setFinishedPerform' to cancel, but isActive isnt set to false, needs logic fix

	public EntityLivingBase owner; //this should be allowed to be null, entityless abilities should be possible

	//Settings
	public String name = "";
	public int ticksToPerform = 10;
	public int ticksToCharge = 0;
	public int ticksToCooldown = 10; //this cooldown is factored into the 'active' use of skill, so it locks out regular melee usage for AI, maybe players
	public float bestDist = 10;
	public float bestDistRange = 10; //the acceptable range around bestDist
	public int type = 0;

	//Runtime
	public int curTickPerform = 0;
	public int curTickCharge = 0;
	public int curTickCooldown = 0;

	private boolean isActive = false; //does not account for cooldown anymore
	private boolean isCoolingDown = false;
	public IAbilityUsageCallback callback;

	public static int TYPE_MELEE = 0;
	public static int TYPE_RANGED = 1;
	public static int TYPE_SUPPORT = 2; //?
	public static int TYPE_MISC = 3;

	//Random helpers
	public boolean hasAppliedDamage = false;
	public int usageCount = 0;

	//central particle use
	@SideOnly(Side.CLIENT)
	public ParticleBehaviorCharge particleBehavior;

	public Ability() {
	}

	public Ability init(EntityLivingBase parOwner) {
		this.owner = parOwner;
		return this;
	}

	public void tick() {

		//particle ticking
		if (owner.world.isRemote) {
			if (particleBehavior == null) {
				particleBehavior = new ParticleBehaviorCharge(new Vec3(owner.posX, owner.posY, owner.posZ));
				particleBehavior.sourceEntity = owner;
			} else {
				particleBehavior.tickUpdateList();
			}
		}

		//System.out.println("Ability.tick(), isRemote: " + owner.world.isRemote + " - name: " + name + " - charge: " + curTickCharge + " - perform: " + curTickPerform + " - cooldown: " + curTickCooldown);

		if (curTickCharge < ticksToCharge) {
			curTickCharge++;
			tickChargeUp();
		} else {
			if (curTickPerform < ticksToPerform) {
				curTickPerform++;
				tickPerform();
			} else {
				if (curTickCooldown == 0) {
					//for performance callback trigger
					setFinishedPerform();
				}
				if (curTickCooldown < ticksToCooldown) {
					curTickCooldown++;
					tickCooldown();
				} else {
					setFinishedEntirely();
				}
			}
		}
	}

	public void tickChargeUp() {
		if (particleBehavior != null) {
			particleBehavior.curTick = curTickCharge;
			particleBehavior.ticksMax = ticksToCharge;
		}
	}

	public void tickPerform() {

	}

	public void tickCooldown() {

	}

	@SideOnly(Side.CLIENT)
	public void tickRender(Render parRender) {

	}

	@SideOnly(Side.CLIENT)
	public void tickRenderModel(ModelBase parModel) {

	}

	public void setFinishedChargeUp() {
		curTickCharge = ticksToCharge;
	}

	public void setFinishedPerform() {
		//System.out.println("perform finish - " + owner.world.isRemote);
		curTickPerform = ticksToPerform;
		isActive = false;
		isCoolingDown = true;

		//moved here so callbacks can release active used skill upon perform finish instead of cooldown finish
		//its assumed the callback class checks for proper canActivate to prevent using a skill thats still cooling down
		//if (isActiveOrCoolingDown()) {
			//isActive = false; //make sure its set to false for sync info
			if (callback != null) callback.abilityFinished(this);
		//}
	}

	public void setFinishedCooldown() {
		curTickCooldown = ticksToCooldown;
		isCoolingDown = false;
	}

	//called from skill invoker if relevant target, for overriding in skills that have targets
	public void setTarget(Entity parTarget) {

	}

	public void setCallback(IAbilityUsageCallback parCallback) {
		callback = parCallback;
	}

	public boolean isActive() {
		return isActive;
	}

	public boolean isActiveOrCoolingDown() {
		return isActive || (isCoolingDown/*curTickCooldown < ticksToCooldown && curTickCooldown != 0*/);
	}

	public boolean canActivate() {
		return !isActive && !isCoolingDown;
	}

	public boolean canHitCancel(DamageSource parSource) {
		return true;
	}

	public void setFinishedEntirely() {

		//System.out.println("finished ability!");

		//incase something bypasses setFinishedPerform()
		if (isActiveOrCoolingDown()) {
			if (callback != null) callback.abilityFinished(this);
		}

		isActive = false;
		isCoolingDown = false;
		reset();
	}

	public void setActive() {
		if (!isActive) {
			usageCount++;
		}
		isActive = true;
	}

	public void reset() {
		curTickPerform = 0;
		curTickCharge = 0;
		curTickCooldown = 0;
		hasAppliedDamage = false;
	}

	//For loading from disk and first time full syncing data to client

	public void nbtLoad(NBTTagCompound nbt) {
		name = nbt.getString("name");
		type = nbt.getInteger("type");
	}

	public NBTTagCompound nbtSave() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("name", name);
		nbt.setString("classname", this.getClass().getCanonicalName());
		nbt.setInteger("type", type);
		nbt.setBoolean("fullSave", true); //lets client side know if it can try to do a full load (more of a safety)
		return nbt;
	}

	//For constant sync to client, runtime updates

	public void nbtSyncRead(NBTTagCompound nbt) {
		name = nbt.getString("name");
		usageCount = nbt.getInteger("usageCount");
		boolean wasActive = isActive;
		isActive = nbt.getBoolean("isActive");
		isCoolingDown = nbt.getBoolean("isCoolingDown");
		if (!isActive && wasActive) setFinishedEntirely();
		curTickCharge = nbt.getInteger("curTickCharge");
		curTickPerform = nbt.getInteger("curTickPerform");
		curTickCooldown = nbt.getInteger("curTickCooldown");
	}

	public NBTTagCompound nbtSyncWrite() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("name", name);
		nbt.setString("classname", this.getClass().getCanonicalName());
		nbt.setInteger("usageCount", usageCount);
		nbt.setBoolean("isActive", isActive);
		nbt.setBoolean("isCoolingDown", isCoolingDown);
		nbt.setInteger("curTickCharge", curTickCharge);
		nbt.setInteger("curTickPerform", curTickPerform);
		nbt.setInteger("curTickCooldown", curTickCooldown);
		return nbt;
	}
}
