package CoroUtil.ability;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class Ability {

	//Times to send nbt to client:
	//- Initial (full disk sync)
	//- New Ability add (full disk sync)
	//- runtime state update (new tick state)
	
	//If ability gets interrupted for state, or goes to next tick, send an update, that tells client its on the next stage at the right time
	
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
	
	private boolean isActive = false;
	public IAbilityUsageCallback callback;
	
	public static int TYPE_MELEE = 0;
	public static int TYPE_RANGED = 1;
	public static int TYPE_SUPPORT = 2; //?
	public static int TYPE_MISC = 3;
	
	//Random helpers
	public boolean hasAppliedDamage = false;
	public int usageCount = 0;
	
	public Ability() {
	}
	
	public Ability init(EntityLivingBase parOwner) {
		this.owner = parOwner;
		return this;
	}

	public void tick() {
		if (curTickCharge < ticksToCharge) {
			curTickCharge++;
			tickChargeUp();
		} else {
			if (curTickPerform < ticksToPerform) {
				curTickPerform++;
				tickPerform();
			} else {
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
		
	}
	
	public void tickPerform() {
		
	}
	
	public void tickCooldown() {
		
	}
	
	@SideOnly(Side.CLIENT)
	public void tickRender(ModelBase model) {
		
	}
	
	public void setFinishedChargeUp() {
		curTickCharge = ticksToCharge;
	}
	
	public void setFinishedPerform() {
		curTickPerform = ticksToPerform;
	}
	
	public void setFinishedCooldown() {
		curTickCooldown = ticksToCooldown;
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
	
	public void setFinishedEntirely() {
		//prevent multiple callback firings
		if (isActive) {
			isActive = false; //make sure its set to false for sync info
			if (callback != null) callback.abilityFinished(this);
		}
		isActive = false;
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
		nbt.setInteger("type", type);
		nbt.setBoolean("fullSave", true); //lets client side know if it can try to do a full load (more of a safety)
		return nbt;
	}
	
	//For constant sync to client, runtime updates
	
	public void nbtSyncRead(NBTTagCompound nbt) {
		name = nbt.getString("name");
		usageCount = nbt.getInteger("usageCount");
		isActive = nbt.getBoolean("isActive");
		curTickCharge = nbt.getInteger("curTickCharge");
		curTickPerform = nbt.getInteger("curTickPerform");
		curTickCooldown = nbt.getInteger("curTickCooldown");
	}
	
	public NBTTagCompound nbtSyncWrite() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("name", name);
		nbt.setInteger("usageCount", usageCount);
		nbt.setBoolean("isActive", isActive);
		nbt.setInteger("curTickCharge", curTickCharge);
		nbt.setInteger("curTickPerform", curTickPerform);
		nbt.setInteger("curTickCooldown", curTickCooldown);
		return nbt;
	}
}
