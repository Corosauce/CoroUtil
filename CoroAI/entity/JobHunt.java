package CoroAI.entity;

import java.util.List;

import CoroAI.PFQueue;

import net.minecraft.src.*;

public class JobHunt extends JobBase {
	
	public long huntRange = 24;
	public boolean dontStray = true;
	public boolean xRay = false;
	
	public JobHunt(JobManager jm) {
		super(jm);
	}
	
	@Override
	public void tick() {
		jobHunter();
	}
	
	@Override
	public boolean shouldExecute() {
		return true;
	}
	
	@Override
	public boolean shouldContinue() {
		return ent.entityToAttack == null;
	}

	@Override
	public void onLowHealth() {
		//if (this.name.equals("Makani")) {
		
		//}
		if (hitAndRunDelay == 0 && ent.getDistanceToEntity(ent.lastFleeEnt) > 3F) {
			hitAndRunDelay = ent.cooldown_Ranged+1;
			ent.entityToAttack = ent.lastFleeEnt;
			if (ent.entityToAttack != null) {
				//ent.faceEntity(ent.entityToAttack, 180F, 180F);
				//ent.rightClickItem();
				//ent.attackEntity(ent.entityToAttack, ent.getDistanceToEntity(ent.entityToAttack));
				//System.out.println("H&R " + ent.name + " health: " + ent.getHealth());
			}
		} else {
			ent.entityToAttack = null;
		}
	}
	
	@Override
	public void hitHook(DamageSource ds, int damage) {
		if (ent.isEnemy(ds.getEntity())) {
			ent.entityToAttack = ds.getEntity();
		}
		
		if (ent.getHealth() < ent.getMaxHealth() / 2 && ds.getEntity() == c_CoroAIUtil.getFirstPlayer()) {
			ent.dipl_hostilePlayer = true;
			ent.getGroupInfo(EnumInfo.DIPL_WARN);
		}
		 
		
		//temp fun code
		/*if (ds.getEntity() instanceof ZCSdkEntitySentry) {
			ent.entityToAttack = ds.getEntity();
		}*/
	}
	
	@Override
	public void setJobItems() {
		
		c_CoroAIUtil.setItems_JobHunt(ent);
		
		
	}
	
	protected void jobHunter() {
	
		dontStray = false;
		
		//this whole function is crap, redo it bitch
		
		//a use for the states
		
		//responding to alert, so you know to cancel it if alert entity / active target is dead
		
		/*if (tryingToFlee && (onGround || isInWater())) {
			tryingToFlee = false;
			fleeFrom(lastFleeEnt);
		}*/
		
		//huntRange = 24;
		ent.maxDistanceFromHome = 48F;
		
		
		//if (true) return;
		
		//health = 8;
		/*if (health < getMaxHealth() * 0.75F) {
			avoid();
			if (rand.nextInt(5) == 0) entityToAttack = null;
		} else {*/
			setJobState(EnumJobState.IDLE);
			
			if (ent.getHealth() > ent.getMaxHealth() * 0.90F && (ent.entityToAttack == null || ent.rand.nextInt(20) == 0)) {
				boolean found = false;
				Entity clEnt = null;
				float closest = 9999F;
		    	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(huntRange, huntRange/2, huntRange));
		        for(int j = 0; j < list.size(); j++)
		        {
		            Entity entity1 = (Entity)list.get(j);
		            if(ent.isEnemy(entity1))
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
		        	ent.huntTarget(clEnt);
		        }
		        /*if (!found) {
		        	setState(EnumKoaActivity.IDLE);
		        }*/
			} else {
				
				if (ent.entityToAttack != null) {
					if (!ent.hasPath() && ent.getDistanceToEntity(ent.entityToAttack) > 5F) {
						PFQueue.getPath(ent, ent.entityToAttack, ent.maxPFRange);
					}
				}
				
			}
			
			//derp
			/*if (ent.entityToAttack == null && ent.rand.nextInt(6000) == 0) {
				ent.walkTo(ent, ent.homeX, ent.homeY, ent.homeZ, ent.maxPFRange, 600);
			}*/
			
		//}
		ent.prevHealth = ent.getHealth();
	}
	
	
	
	public void hunterHitHook(DamageSource ds, int damage) {
		
		/*if (health < getMaxHealth() / 4 * 3) {
			if (ds.getEntity() != null) {
				lastFleeEnt = ds.getEntity();
				tryingToFlee = true;
				//fleeFrom(ds.getEntity());
			}
		}
		prevKoaHealth = health;*/
	}
	
	public boolean sanityCheckHelp(Entity caller, Entity target) {
		if (ent.getHealth() < 10) {
			return false;
		}
		
		if (dontStray) {
			if (target.getDistance(ent.homeX, ent.homeY, ent.homeZ) > ent.maxDistanceFromHome * 1.5) {
				return false;
			}
		}
		if (ent.rand.nextInt(2) == 0) {
			return true;
		}
		return false;
	}
	
	public boolean sanityCheck(Entity target) {
		if (ent.getHealth() < 10) {
			return false;
		}
		
		if (dontStray) {
			if (target.getDistance(ent.homeX, ent.homeY, ent.homeZ) > ent.maxDistanceFromHome) {
				return false;
			}
		}
		return true;
	}
	
	
	
}
