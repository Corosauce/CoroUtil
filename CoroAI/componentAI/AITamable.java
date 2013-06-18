package CoroAI.componentAI;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import scala.util.Random;
import CoroAI.c_CoroAIUtil;
import CoroAI.componentAI.jobSystem.JobBase;

public class AITamable {

	public JobBase job;
	public String owner = "";
	public int ownerEntityID = -1; //for non players, should use UUID
	public ChunkCoordinates occupyCoord;
	public double followDistMin = 2D;
	public double followDistMax = 8D;
	public boolean overrideNonPlayerTargetting = true;
	
	public AITamable(JobBase parJob) {
		job = parJob;
	}
	
	public boolean isTame() {
		return !owner.equals("");
	}
	
	public boolean isEnemy(Entity ent) {
		if (ent instanceof EntityPlayer) return false;
		
		//use a cached list of class types here to compare against, this list a player can add to for mod entities to not attack
		return DiplomacyHelper.shouldTargetEnt(job.ent, ent, true);
		
		//return job.entInt.isEnemy(ent);
	}
	
	public void tick() {
		if (isTame()) {
			EntityLiving ent = job.ent.worldObj.getPlayerEntityByName(owner);
			if (ent != null) {
				occupyCoord = c_CoroAIUtil.entToCoord(ent);
				
				if (job.ai.entityToAttack != null) {
					if (job.ai.entityToAttack.entityId == ent.entityId || !DiplomacyHelper.shouldTargetEnt(job.ent, job.ai.entityToAttack, true)) {
						job.ai.entityToAttack = null;
					}
				}
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
    			if (job.ai.useInv) job.lookForItems();
        	}
        }
	}
}
