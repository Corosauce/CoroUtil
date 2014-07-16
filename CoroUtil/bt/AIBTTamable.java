package CoroUtil.bt;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import CoroUtil.OldUtil;
import CoroUtil.util.CoroUtilEntity;

public class AIBTTamable {

	//i think this classes ticking/follow owner routine would work best in the form of an order dispatched on the side
	
	public AIBTAgent agent;
	public String owner = ""; //when set to "", means not tame
	public int ownerEntityID = -1; //for non players, should use UUID
	public EntityLivingBase ownerCachedInstance = null;
	public ChunkCoordinates occupyCoord;
	public double followDistMin = 2D;
	public double followDistMax = 8D;
	public double strayDistMax = 32D;
	public double teleportFromFarDist = 18D;
	//public boolean overrideNonPlayerTargetting = true;
	
	//linking pets routines to AI targetting and wander
	public boolean shouldStayStill = false;
	
	public AIBTTamable(AIBTAgent parJob) {
		agent = parJob;
	}
	
	public boolean isTame() {
		return !owner.equals("");
	}
	
	public boolean shouldStayStill() {
		return shouldStayStill;
	}
	
	public void setTamedByOwner(String user) {
		owner = user;
		updateCache();
	}
	
	public void tameClear() {
		owner = "";
		ownerCachedInstance = null;
	}
	
	public void updateCache() {
		ownerCachedInstance = agent.ent.worldObj.getPlayerEntityByName(owner);
	}
	
	public EntityLivingBase getPlayerCached() {
		return ownerCachedInstance;
	}
	
	public boolean isEnemy(Entity ent) {
		if (ent instanceof EntityPlayer) {
			if (CoroUtilEntity.getName(ent).equals(owner)) {
				return false;
			}
		}
		
		//use a cached list of class types here to compare against, this list a player can add to for mod entities to not attack
		return false;//DiplomacyHelper.shouldTameTargetEnt(agent.ent, ent, this);
		
		//return job.entInt.isEnemy(ent);
	}
	
	public void tick() {
		if (isTame()) {
			updateCache();
			EntityLivingBase ent = getPlayerCached();
			if (ent != null) {
				
				occupyCoord = OldUtil.entToCoord(ent);
				
				if (!shouldStayStill() && (ent.onGround || ent.isInWater()) && teleportFromFarDist != -1 && agent.ent.getDistanceToEntity(ent) > teleportFromFarDist) {
					double range = 0D;
					Random rand = new Random();
					agent.ent.setPosition(ent.posX + (rand.nextDouble() * range) - (rand.nextDouble() * range), ent.posY, ent.posZ + (rand.nextDouble() * range) - (rand.nextDouble() * range));
					agent.ent.fallDistance = -100;
					agent.blackboard.setMoveTo(null, true);
					agent.blackboard.setTarget(null);
				}
				
				//Target fixing
				/*if (agent.blackboard.getTarget() != null) {
					if (agent.blackboard.getTarget().entityId == ent.entityId || !DiplomacyHelper.shouldTameTargetEnt(agent.ent, agent.blackboard.getTarget(), this)) {
						agent.blackboard.setTarget(null);
					}
				}*/
				
				if (!agent.ent.isPotionActive(Potion.confusion.id)) agent.ent.addPotionEffect(new PotionEffect(Potion.confusion.id, 5, 0));
			}
		}
	}
	
	public void onIdleTick() {
		/*Random rand = new Random();
		if(((agent.ent.getNavigator().noPath()) && rand.nextInt(40) == 0))
        {
			if (occupyCoord == null || OldUtil.getDistanceXZ(agent.ent, occupyCoord) < followDistMax) {
				job.ai.updateWanderPath();
			} else {
				job.ai.walkTo(job.ent, occupyCoord, job.ai.maxPFRange, 600);
				int randsize = 8;
        		job.ai.walkTo(ent, ai.homeX+rand.nextInt(randsize) - (randsize/2), ai.homeY+1, ai.homeZ+rand.nextInt(randsize) - (randsize/2),ai.maxPFRange, 600);
			}
		} else {
        	if (job.ent.getNavigator().noPath()) {
        		if (job.ai.useInv && job.ai.entInv.shouldLookForPickups) job.lookForItems();
        	}
        }*/
	}
	
	public void cleanup() {
		agent = null;
		ownerCachedInstance = null;
	}
}
