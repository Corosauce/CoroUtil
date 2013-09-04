package CoroAI.componentAI;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import CoroAI.c_CoroAIUtil;
import CoroAI.componentAI.jobSystem.JobBase;
import CoroAI.diplomacy.DiplomacyHelper;

public class AITamable {

	public JobBase job;
	public String owner = "";
	public int ownerEntityID = -1; //for non players, should use UUID
	public EntityLivingBase ownerCachedInstance = null;
	public ChunkCoordinates occupyCoord;
	public double followDistMin = 2D;
	public double followDistMax = 8D;
	public double strayDistMax = 32D;
	public double teleportFromFarDist = 48D;
	public boolean overrideNonPlayerTargetting = true;
	
	public AITamable(JobBase parJob) {
		job = parJob;
	}
	
	public boolean isTame() {
		return !owner.equals("");
	}
	
	public void tameBy(String user) {
		owner = user;
		updateCache();
	}
	
	public void tameClear() {
		owner = "";
		ownerCachedInstance = null;
	}
	
	public void updateCache() {
		ownerCachedInstance = job.ent.worldObj.getPlayerEntityByName(owner);
	}
	
	public EntityLivingBase getPlayerCached() {
		return ownerCachedInstance;
	}
	
	public boolean isEnemy(Entity ent) {
		if (ent instanceof EntityPlayer) {
			if (((EntityPlayer)ent).username.equals(owner)) {
				return false;
			}
		}
		
		//use a cached list of class types here to compare against, this list a player can add to for mod entities to not attack
		return DiplomacyHelper.shouldTameTargetEnt(job.ent, ent, this);
		
		//return job.entInt.isEnemy(ent);
	}
	
	public void tick() {
		if (isTame()) {
			updateCache();
			EntityLivingBase ent = getPlayerCached();
			if (ent != null) {
				occupyCoord = c_CoroAIUtil.entToCoord(ent);
				
				if ((ent.onGround || ent.isInWater()) && teleportFromFarDist != -1 && job.ai.ent.getDistanceToEntity(ent) > teleportFromFarDist) {
					double range = 2D;
					Random rand = new Random();
					job.ai.ent.setPosition(ent.posX + (rand.nextDouble() * range) - (rand.nextDouble() * range), ent.posY, ent.posZ + (rand.nextDouble() * range) - (rand.nextDouble() * range));
					job.ai.ent.getNavigator().clearPathEntity();
				}
				
				//Target fixing
				if (job.ai.entityToAttack != null) {
					if (job.ai.entityToAttack.entityId == ent.entityId || !DiplomacyHelper.shouldTameTargetEnt(job.ent, job.ai.entityToAttack, this)) {
						job.ai.entityToAttack = null;
					}
				}
				
				if (!job.ai.ent.isPotionActive(Potion.confusion.id)) job.ai.ent.addPotionEffect(new PotionEffect(Potion.confusion.id, 5, 0));
			}
		}
	}
	
	public void onIdleTick() {
		Random rand = new Random();
		if(((job.ent.getNavigator().noPath()) && rand.nextInt(40) == 0))
        {
			if (occupyCoord == null || c_CoroAIUtil.getDistanceXZ(job.ent, occupyCoord) < followDistMax) {
				job.ai.updateWanderPath();
			} else {
				job.ai.walkTo(job.ent, occupyCoord, job.ai.maxPFRange, 600);
				/*int randsize = 8;
        		job.ai.walkTo(ent, ai.homeX+rand.nextInt(randsize) - (randsize/2), ai.homeY+1, ai.homeZ+rand.nextInt(randsize) - (randsize/2),ai.maxPFRange, 600);*/
			}
		} else {
        	if (job.ent.getNavigator().noPath()) {
        		if (job.ai.useInv && job.ai.entInv.shouldLookForPickups) job.lookForItems();
        	}
        }
	}
	
	public void cleanup() {
		job = null;
		ownerCachedInstance = null;
	}
}
