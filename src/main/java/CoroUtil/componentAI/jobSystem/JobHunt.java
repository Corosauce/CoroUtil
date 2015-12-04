package CoroUtil.componentAI.jobSystem;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import CoroUtil.entity.EnumJobState;

public class JobHunt extends JobBase {
	
	public long huntRange = 24;
	
	public boolean xRay = false;
	
	public JobHunt(JobManager jm) {
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
		if (hitAndRunDelay == 0 && ent.getDistanceToEntity(ai.lastFleeEnt) > 3F) {
			hitAndRunDelay = entInt.getCooldownRanged()+1;
			ai.entityToAttack = ai.lastFleeEnt;
			if (ai.entityToAttack != null) {
				ai.faceEntity(ai.entityToAttack, 180F, 180F);
				/*if (ai.useInv) {
					ai.entInv.attackRanged(ai.entityToAttack, ent.getDistanceToEntity(ai.lastFleeEnt));
    			} else {*/
    				entInt.attackRanged(ai.entityToAttack, ent.getDistanceToEntity(ai.lastFleeEnt));
    			//}
			}
		}
	}
	
	@Override
	public boolean hookHit(DamageSource ds, int damage) {
		/*if (isEnemy(ds.getEntity())) {
			ai.entityToAttack = ds.getEntity();
		}*/
		
		if (ai.retaliateEnable) {
        	ai.setTargetRetaliate(ds.getEntity());
        }
		
		if (ent.getHealth() < ent.getMaxHealth() / 2/* && ds.getEntity() == c_CoroAIUtil.getFirstPlayer()*/) {
			//System.out.println("TEMP OFF FOR REFACTOR");
			/*ai.dipl_hostilePlayer = true;
			ai.getGroupInfo(EnumInfo.DIPL_WARN);*/
		}
		 
		return true;
		//temp fun code
		/*if (ds.getEntity() instanceof ZCSdkEntitySentry) {
			ent.entityToAttack = ds.getEntity();
		}*/
	}
	
	@Override
	public void setJobItems() {
		
		//c_CoroAIUtil.setItems_JobHunt(ai.entInv);
		
		
	}
	
	protected void jobHunter() {
	
		//crappy fix - behavior tree generic locking would solve this reset issue
		/*if (ai.entInv != null && ai.entInv.fishEntity != null) {
			ai.entInv.fishEntity.setDead();
			ai.entInv.fishEntity.catchFish();
			ai.entInv.fishEntity = null;
		}*/
		
		//this whole function is crap, redo it bitch
		
		//a use for the states
		
		//responding to alert, so you know to cancel it if alert entity / active target is dead
		
		/*if (tryingToFlee && (onGround || isInWater())) {
			tryingToFlee = false;
			fleeFrom(lastFleeEnt);
		}*/
		
		//huntRange = 24;
		ai.maxDistanceFromHome = 48F;
		
		
		//if (true) return;
		
		//health = 8;
		/*if (health < getMaxHealth() * 0.75F) {
			avoid();
			if (rand.nextInt(5) == 0) entityToAttack = null;
		} else {*/
			setJobState(EnumJobState.IDLE);
			
			EntityLivingBase protectEnt = ent;
			if (tamable.isTame()) {
				EntityPlayer entP = ent.worldObj.getPlayerEntityByName(tamable.owner);
				if (entP != null) protectEnt = entP; 
			}
			
			if (/*ent.getHealth() > ent.getMaxHealth() * 0.90F &&*/ (ai.entityToAttack == null || ai.rand.nextInt(20) == 0)) {
				boolean found = false;
				Entity clEnt = null;
				float closest = 9999F;
		    	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, protectEnt.getEntityBoundingBox().expand(huntRange, huntRange/2, huntRange));
		        for(int j = 0; j < list.size(); j++)
		        {
		            Entity entity1 = (Entity)list.get(j);
		            if(isEnemy(entity1))
		            {
		            	if (xRay || ((EntityLivingBase) entity1).canEntityBeSeen(protectEnt)) {
		            		if (sanityCheck(entity1)/* && entity1 instanceof EntityPlayer*/) {
		            			float dist = protectEnt.getDistanceToEntity(entity1);
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
		        		ai.huntTarget(clEnt);
		        	} else {
		        		//if (ent.getNavigator().noPath()) {
		        			ai.huntTarget(clEnt);
		        		//}
		        	}
		        	
		        }
		        /*if (!found) {
		        	setState(EnumKoaActivity.IDLE);
		        }*/
			} else {
				
				if (ai.entityToAttack != null) {
					if (ent.getNavigator().noPath() && ent.worldObj.getWorldTime() % 10 == 0/* && ent.getDistanceToEntity(ai.entityToAttack) > 5F*/) {
						ai.huntTarget(ai.entityToAttack);
						/*if (ent.isInWater() || !isInFormation() || ai.activeFormation.leader == entInt) */
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
	
	public boolean sanityCheckHelp(Entity caller, Entity target) {
		if (ai.shouldAvoid && ent.getHealth() < 10) {
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
		if (ai.shouldAvoid && ent.getHealth() < 6) {
			return false;
		}
		
		if (dontStrayFromHome) {
			if (target.getDistance(ai.homeX, ai.homeY, ai.homeZ) > ai.maxDistanceFromHome) {
				return false;
			}
		}
		
		if (dontStrayFromOwner && this.tamable.isTame()) {
			EntityPlayer entP = ai.ent.worldObj.getPlayerEntityByName(tamable.owner);
			if (entP != null) {
				if (entP.getDistanceToEntity(target) > tamable.strayDistMax) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	
}
