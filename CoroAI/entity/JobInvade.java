package CoroAI.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

import java.util.List;

import CoroAI.PFQueue;

/* JobInvade: For Omnipotent focused invasions */

//NOTE TO SELF: Uses player list for if no close target, huntRange for close frequently called distance based targetting 

public class JobInvade extends JobBase {
	
	public long huntRange = 32;
	public boolean omnipotent = true;
	
	public int retargetDelayCount = 0;
	public int retargetDelay = 20;
	public int retargetDist = 10;
	
	public JobInvade(JobManager jm) {
		super(jm);
		
		//let them be spawned for a bit before targetting
		retargetDelayCount = retargetDelay;
	}
	
	@Override
	public void tick() {
		
		if (retargetDelayCount > 0) retargetDelayCount--;
		
		jobHunter();
	}

	@Override //never called if avoid() returns false
	public void onLowHealth() {
		super.onLowHealth();
		/*if (hitAndRunDelay == 0 && ent.getDistanceToEntity(ent.lastFleeEnt) > 6F) {
			hitAndRunDelay = ent.cooldown_Ranged+1;
			ent.entityToAttack = ent.lastFleeEnt;
			if (ent.entityToAttack != null) ent.faceEntity(ent.entityToAttack, 180F, 180F);
		} else {
			ent.entityToAttack = null;
		}*/
	}
	
	@Override
	public void hitHook(DamageSource ds, int damage) {
		if (ent.isEnemy(ds.getEntity())) {
			ent.entityToAttack = ds.getEntity();
		}
		
		//temp fun code
		/*if (ds.getEntity() instanceof ZCSdkEntitySentry) {
			ent.entityToAttack = ds.getEntity();
		}*/
	}
	
	@Override
	public void setJobItems() {
		
		//Melee slot
		//ent.inventory.addItemStackToInventory(new ItemStack(Item.swordWood, 1));
		//Ranged slot
		
		int choice = ent.rand.nextInt(2);
		if (choice == 0) {
			//ent.inventory.addItemStackToInventory(new ItemStack(mod_SdkGuns.itemGunAk47, 1));
			//ent.inventory.addItemStackToInventory(new ItemStack(mod_SdkGuns.itemBulletLight, 64));
		} else if (choice == 1) {
			//ent.inventory.addItemStackToInventory(new ItemStack(mod_SdkGuns.itemGunFlamethrower, 1));
			//ent.inventory.addItemStackToInventory(new ItemStack(mod_SdkGuns.itemOil, 64));
		}
	}
	
	@Override
	public boolean avoid(boolean actOnTrue) {
		return false;
	}
	
	@Override
	public boolean checkHunger() {
		
		return false;
	}
	
	protected void jobHunter() {
	
		
		
		//this whole function is crap, redo it bitch
		
		//a use for the states
		
		//responding to alert, so you know to cancel it if alert entity / active target is dead
		
		/*if (tryingToFlee && (onGround || isInWater())) {
			tryingToFlee = false;
			fleeFrom(lastFleeEnt);
		}*/
		
		
		
		//retargetDelay = 20;
		
		
		//if (true) return;
		
		//health = 8;
		/*if (health < getMaxHealth() * 0.75F) {
			avoid();
			if (rand.nextInt(5) == 0) entityToAttack = null;
		} else {*/
			//setJobState(EnumJobState.IDLE);
			Entity clEnt = null;
			
		
			if (/*ent.getEntityHealth() > ent.getMaxHealth() * 0.90F && */(ent.entityToAttack == null || ent.rand.nextInt(20) == 0)) {
				boolean found = false;
				
				float closest = 9999F;
		    	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(huntRange, huntRange/2, huntRange));
		        for(int j = 0; j < list.size(); j++)
		        {
		            Entity entity1 = (Entity)list.get(j);
		            if(ent.isEnemy(entity1))
		            {
		            	//if (((EntityLiving) entity1).canEntityBeSeen(ent)) {
		            		if (sanityCheck(entity1) && entity1 instanceof EntityLiving && ((EntityLiving)entity1).getHealth() > 0) {
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
		            	//}
		            }
		        }
		        if (clEnt != null) {
		        	//THIS IS WHERE YOU SHOULD PUT LOGIC CHECK FOR EQUAL HEIGHT or something
		        	
		        	if (ent.isSolidPath(clEnt)) {
		        		
		        		if (!ent.isBreaking()) {
		        			ent.huntTarget(clEnt, -1);
		        		}
		        		//System.out.println("huntTarget instant");
		        	} else {
		        		float dist = clEnt.getDistanceToEntity(ent);
		        		if (retargetDelayCount == 0 && ((dist < retargetDist && dist > 2F) || ent.entityToAttack == null)) {
		        			//Only retarget if they can be seen, to prevent weird long distance pf derps?
		        			
		        			if (ent.getNavigator().noPath() || ((EntityLiving) clEnt).canEntityBeSeen(ent)) {
		        				retargetDelayCount = retargetDelay;
		        				if (!ent.isBreaking()) {
		        					ent.huntTarget(clEnt);
		        				}
		        			}
		        			
		        		}
		        	}
		        } else {
		        	
		        }
		        /*if (!found) {
		        	setState(EnumKoaActivity.IDLE);
		        }*/
			} else {
				
				if (ent.entityToAttack != null) {
					
					float dist = ent.getDistanceToEntity(ent.entityToAttack);
					//ent.getLookHelper().setLookPositionWithEntity(ent.entityToAttack, 10.0F, (float)ent.getVerticalFaceSpeed());
					
					if (((ent.getNavigator().noPath()) && (retargetDelayCount == 0 && dist > 2F/* && ent.entityToAttack.getDistanceToEntity(ent) < retargetDist*/))/* && ent.getDistanceToEntity(ent.entityToAttack) > 5F*/) {
						retargetDelayCount = retargetDelay;
						if (!ent.isBreaking()) {
							if (PFQueue.getPath(ent, ent.entityToAttack, ent.maxPFRange)) {
								//System.out.println("huntTarget repath");
							}
						}
					}
				}
				
			}
			
			if (clEnt == null && ent.entityToAttack == null) {
				//GET PLAYER SINCE NO CLOSE TARGETS!!!!!
	        	EntityPlayer entP = getClosestPlayerToEntity(ent, -1F, false);
	        	if (entP != null && entP.getHealth() > 0) {
		        	if (ent.getNavigator().noPath()) {
		        		//System.out.println("huntTarget far");
	        			ent.huntTarget(entP);
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
	
	
	public boolean sanityCheck(Entity target) {
		
		if (true) return true;
		
		if (ent.getHealth() < 10) {
			return false;
		}
		
		if (target.getDistance(ent.homeX, ent.homeY, ent.homeZ) > ent.maxDistanceFromHome) {
			return false;
		}
		return true;
	}
	
	
	
}
