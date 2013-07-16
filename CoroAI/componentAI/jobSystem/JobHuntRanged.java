package CoroAI.componentAI.jobSystem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;

import java.util.List;

import CoroAI.PFQueue;
import CoroAI.c_CoroAIUtil;
import CoroAI.entity.EnumJobState;

public class JobHuntRanged extends JobBase {
	
	public long huntRange = 24;
	public long keepDistantRange = 14;
	
	public boolean xRay = false;
	
	public boolean useMelee = false;
	public int useMeleeCountdown = 0;
	public int useMeleeCountdownMax = 80;
	
	public Vec3 targetLastPos = null;
	public int targetNoMoveTicks = 0;
	public int targetNoMoveTicksMax = 4;
	
	public JobHuntRanged(JobManager jm) {
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
		super.onLowHealth();
		//if (this.name.equals("Makani")) {
		
		//}
		//System.out.println("hitAndRunDelay: " + hitAndRunDelay);
		if (hitAndRunDelay == 0 && ent.getDistanceToEntity(ai.lastFleeEnt) > 3F) {
			hitAndRunDelay = entInt.getCooldownRanged()+1;
			ai.entityToAttack = ai.lastFleeEnt;
			if (ai.entityToAttack != null) {
				ent.faceEntity(ai.entityToAttack, 180F, 180F);
				if (ai.useInv) {
					ai.entInv.attackRanged(ai.entityToAttack, ent.getDistanceToEntity(ai.lastFleeEnt));
    			} else {
    				entInt.attackRanged(ai.entityToAttack, ent.getDistanceToEntity(ai.lastFleeEnt));
    			}
				//ent.attackEntity(ent.entityToAttack, ent.getDistanceToEntity(ent.entityToAttack));
				//System.out.println("H&R " + ent.name + " health: " + ent.getHealth());
			}
		} else {
			ai.entityToAttack = null;
		}
	}
	
	@Override
	public boolean shouldTickCloseCombat() {
		if (!useMelee) {
			return false;
		} else {
			return super.shouldTickCloseCombat();
		}
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
	public boolean checkDangers() {
		if (!useMelee && ai.entityToAttack != null && ai.entityToAttack.getDistanceToEntity(ai.ent) < keepDistantRange - 2) {
			//System.out.println("too close!");
			return true; 
		}
		return checkHealth();
	}
	
	protected void jobHunter() {
	
		dontStrayFromHome = false;
		
		//this whole function is crap, redo it bitch
		
		//a use for the states
		
		//responding to alert, so you know to cancel it if alert entity / active target is dead
		
		/*if (tryingToFlee && (onGround || isInWater())) {
			tryingToFlee = false;
			fleeFrom(lastFleeEnt);
		}*/
		
		//huntRange = 24;
		ai.maxDistanceFromHome = 48F;
		
		if (ai.entityToAttack != null && targetLastPos != null) {
			if (ent.worldObj.getWorldTime() % 10 == 0) {
				//System.out.println(ai.entityToAttack.getDistance(targetLastPos.xCoord, targetLastPos.yCoord, targetLastPos.zCoord));
				if (ai.entityToAttack.getDistance(targetLastPos.xCoord, targetLastPos.yCoord, targetLastPos.zCoord) < 0.5D) {
					targetNoMoveTicks++;
				} else {
					targetNoMoveTicks = 0;
				}
			}
			
			if (targetNoMoveTicks >= targetNoMoveTicksMax) {
				useMeleeCountdown = useMeleeCountdownMax;
				//System.out.println("attack! " + targetNoMoveTicks + " - " + useMelee);
			}
		} else {
			useMeleeCountdown = 0;
		}
		
		if (useMeleeCountdown > 0) {
			useMeleeCountdown--;
			useMelee = true;
		} else {
			useMelee = false;
		}
		
		setJobState(EnumJobState.IDLE);
		
		if (ent.getHealth() > ent.getMaxHealth() * 0.90F && (ai.entityToAttack == null || ai.rand.nextInt(20) == 0)) {
			boolean found = false;
			Entity clEnt = null;
			float closest = 9999F;
	    	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(huntRange, huntRange/2, huntRange));
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);
	            if(isEnemy(entity1))
	            {
	            	if (xRay || ((EntityLiving) entity1).canEntityBeSeen(ent)) {
	            		if (sanityCheck(entity1)/* && entity1 instanceof EntityPlayer*/) {
	            			float dist = ent.getDistanceToEntity(entity1);
	            			if (dist < closest) {
	            				closest = dist;
	            				clEnt = entity1;
	            			}
		            		
		            		//found = true;
		            		//break;
	            		}
	            		//this.hasAttacked = true;
	            		//getPathOrWalkableBlock(entity1, 16F);
	            	}
	            }
	        }
	        if (clEnt != null) {
	        	if (ai.entityToAttack != clEnt) {
	        		ai.setTarget(clEnt);
	        	} else {
	        		//if (ent.getNavigator().noPath()) {
	        			ai.setTarget(clEnt);
	        		//}
	        	}
	        	
	        }
	        /*if (!found) {
	        	setState(EnumKoaActivity.IDLE);
	        }*/
		} else {
			
			if (ai.entityToAttack != null) {
				if (!useMelee) {
					if (ai.entityToAttack.getDistanceToEntity(ent) < keepDistantRange) {
						ent.getNavigator().clearPathEntity();
					}
				}
				if (ent.getNavigator().noPath() && (ent.getDistanceToEntity(ai.entityToAttack) > keepDistantRange + 1 || useMelee)) {
					PFQueue.getPath(ent, ai.entityToAttack, ai.maxPFRange);
				} else if (!useMelee && !ai.fleeing) {
					if (ai.entityToAttack.getDistanceToEntity(ent) < keepDistantRange) {
						ent.getNavigator().clearPathEntity();
					}
				}
			}
			
		}
		if (ent.worldObj.getWorldTime() % 10 == 0) {
			if (ai.entityToAttack != null) {
				targetLastPos = Vec3.createVectorHelper(ai.entityToAttack.posX, ai.entityToAttack.posY, ai.entityToAttack.posZ);
			}
		}
		ent.prevHealth = ent.getHealth();
	}
	
	public boolean sanityCheckHelp(Entity caller, Entity target) {
		if (ent.getHealth() < 10) {
			return false;
		}
		
		if (dontStrayFromHome) {
			if (target.getDistance(ai.homeX, ai.homeY, ai.homeZ) > ai.maxDistanceFromHome * 1.5) {
				return false;
			}
		}
		if (ai.rand.nextInt(2) == 0) {
			return true;
		}
		return false;
	}
	
	public boolean sanityCheck(Entity target) {
		if (ent.getHealth() < 6) {
			return false;
		}
		
		if (dontStrayFromHome) {
			if (target.getDistance(ai.homeX, ai.homeY, ai.homeZ) > ai.maxDistanceFromHome) {
				return false;
			}
		}
		return true;
	}
	
	
	
}
