package CoroUtil.componentAI.jobSystem;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import CoroUtil.entity.EnumJobState;

public class JobHuntTurret extends JobBase {
	
	public long huntRange = 20;
	
	public boolean xRay = false;
	
	public boolean useMelee = false;
	
	public JobHuntTurret(JobManager jm) {
		super(jm);
	}
	
	@Override
	public void tick() {
		super.tick();
		jobHunter();
	}
	
	@Override
	public boolean shouldExecute() {
		return true;
	}
	
	@Override
	public boolean shouldContinue() {
		return ai.entityToAttack == null || ai.entityToAttack.getDistanceToEntity(ent) > huntRange;
	}

	@Override
	public void onLowHealth() {
		
	}
	
	@Override
	public boolean shouldTickCloseCombat() {
		return false;
	}
	
	@Override
	public boolean hookHit(DamageSource ds, int damage) {
		if (isEnemy(ds.getEntity())) {
			ai.entityToAttack = ds.getEntity();
		}
		return true;
	}
	
	@Override
	public void setJobItems() {
		
		//c_CoroAIUtil.setItems_JobHunt(ai.entInv);
		
		
	}
	
	@Override
	public boolean avoid(boolean actOnTrue) {
		
		return false;
	}
	
	protected void jobHunter() {
	
		//huntRange = 20;
		
		if (ai.entityToAttack != null && ai.entityToAttack.getDistanceToEntity(ai.ent) > huntRange) ai.entityToAttack = null;
		
		setJobState(EnumJobState.IDLE);
		
		if (ent.worldObj.getWorldTime() % 10 == 0/*(ai.entityToAttack == null || ai.rand.nextInt(20) == 0)*/) {
			boolean found = false;
			Entity clEnt = null;
			float closest = 9999F;
	    	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.getEntityBoundingBox().expand(huntRange, huntRange, huntRange));
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);
	            if(isEnemy(entity1))
	            {
	            	if (xRay || ((EntityLivingBase) entity1).canEntityBeSeen(ent)) {
	            		if (sanityCheck(entity1)/* && entity1 instanceof EntityPlayer*/) {
	            			float dist = ent.getDistanceToEntity(entity1);
	            			if (dist < closest) {
	            				closest = dist;
	            				clEnt = entity1;
	            			}
	            		}
	            	}
	            }
	        }
	        if (clEnt != null) {
	        	if (ai.entityToAttack != clEnt) {
	        		ai.setTarget(clEnt);
	        	} else {
	        		ai.setTarget(clEnt);
	        	}
	        	
	        }
		} else {
			
		}
		//ent.prevHealth = ent.getHealth();
	}
	
	
	
	public void hunterHitHook(DamageSource ds, int damage) {
		
	}
	
	public boolean sanityCheckHelp(Entity caller, Entity target) {
		return false;
	}
	
	public boolean sanityCheck(Entity target) {
		return true;
	}
}
