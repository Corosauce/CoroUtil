package CoroAI.componentAI;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.src.c_CoroAIUtil;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.List;
import java.util.Random;

import CoroAI.PFQueue;
import CoroAI.componentAI.jobSystem.JobManager;
import CoroAI.entity.EnumActState;
import CoroAI.entity.EnumJobState;
import CoroAI.entity.EnumTeam;

public class AIAgent {

	//Main
	public EntityLiving ent;
	public ICoroAI entInt;
	public AIInventory entInv;
	public boolean useInv;
	public JobManager jobMan;
	
	//Path
	public boolean waitingForNewPath;
	public PathEntity pathToEntity;
	
	// AI fields \\
	
	//Attack
	public Entity entityToAttack;
	//public int cooldown_Melee;
	//public int cooldown_Ranged;
	public int curCooldown_Melee;
	public int curCooldown_Ranged;
	public float maxReach_Melee = 2;
	public float maxReach_Ranged = 10;
	
	//Area scanning
	public long checkAreaDelay;
	public long checkRange = 16;
	public int dangerLevel = 0;
	public boolean fleeing = false;
	public Entity lastFleeEnt;
	
	//Proximity based vanilla AI enhancing
	public boolean enhanceAIEnemies = false;
	public int enhancedAIDelay = 100;
	
	//Other fields
	public int maxPFRange = 64;
	public Random rand;
	public int openedChest = 0;
	public float oldMoveSpeed;
	public float fleeSpeed;
	public float lungeFactor = 1.0F;
	public int entID = -1;
	public EnumActState currentAction;
	
	//fields that should be moved to jobs?
	public int homeX;
	public int homeY;
	public int homeZ;
	public int targX;
	public int targY;
	public int targZ;
	public double maxDistanceFromHome = 96;
	public boolean facingWater = false;
	public boolean wasInWater = false;
	
	//Diplomatic fields
	public boolean dipl_hostilePlayer = false;
	public EnumTeam dipl_team = EnumTeam.HOSTILES;
	public int dipl_spreadInfoDelay = 0;
	
	public AIAgent(ICoroAI parEnt, boolean useInventory) {
		ent = (EntityLiving)parEnt;
		entInt = parEnt;
		useInv = useInventory;
		if (useInv) {
			entInv = new AIInventory(this);
		}
		jobMan = new JobManager(this);
		rand = new Random();
		setState(EnumActState.IDLE);
	}
	
	public void initJobs() {
		if (useInv) entInv.initJobs();
		if (entID == -1) entID = rand.nextInt(999999999);
	}
	
	public void setState(EnumActState eka) {
		currentAction = eka;
	}
	
	public void setMoveSpeed(float var) {
		ent.moveSpeed = var;
		oldMoveSpeed = var;
		/*if (!ent.worldObj.isRemote) {
			this.dataWatcher.updateObject(23, Integer.valueOf((int)(var * 1000)));
		}*/
	}
	
	public void updateAITasks() {
		c_CoroAIUtil.addAge(ent, 1);
		c_CoroAIUtil.despawnEntity(ent);
        
        //if (fakePlayer == null) return;
        
        //this.func_48090_aM().func_48481_a();
        //this.targetTasks.onUpdateTasks();
        //this.tasks.onUpdateTasks();
        checkPathfindLock();
        ent.getNavigator().onUpdateNavigation();
        updateAI();
        //this.func_48097_s_();
        ent.getMoveHelper().onUpdateMoveHelper();
        ent.getLookHelper().onUpdateLook();
        ent.getJumpHelper().doJump();
	}
	
	public void doMovement() {
		
		//fix for last node being too high
		if (!ent.worldObj.isRemote) {
			PathEntity pe = ent.getNavigator().getPath();
			if (pe != null) {
				if (pe.getCurrentPathLength() == 1) {
					//if (job.priJob == EnumJob.TRADING) {
						if (pe.getFinalPathPoint().yCoord - ent.posY > 0.01F) {
							//System.out.println(pe.getFinalPathPoint().yCoord - ent.posY);
							ent.getNavigator().clearPathEntity();
						}
					//}
				}
			}
		}
		
		if (ent.isInWater()) {
			//if (ent.jumpDelay == 0) {
				if (entityToAttack != null) {
					//faceEntity(entityToAttack,30F,30F);
					//this.getMoveHelper().setMoveTo(entityToAttack.posX, entityToAttack.posY, entityToAttack.posZ, this.getMoveHelper().getSpeed());
				}
				//jump();
				//this.jumpDelay = 30;
				ent.motionY += 0.03D;
				
				//pathfollow fix
				if (true) {
					if (ent.getNavigator().getPath() != null) {
						PathEntity pEnt = ent.getNavigator().getPath();
						int index = pEnt.getCurrentPathIndex()+1;
						//index--;
						if (index < 0) index = 0;
						if (index > pEnt.getCurrentPathLength()) index = pEnt.getCurrentPathLength()-1;
						Vec3 var1 = null;
						try {
							//var1 = pEnt.getVectorFromIndex(this, index);
							//var1 = pEnt.getVectorFromIndex(this, pEnt.getCurrentPathLength()-1);
							//if (pEnt.getCurrentPathLength() > 2) {
								var1 = pEnt.getVectorFromIndex(ent, pEnt.getCurrentPathIndex());
							//}
						} catch (Exception ex) {
							//System.out.println("c_EnhAI water pf err");
							//ex.printStackTrace();
							var1 = pEnt.getVectorFromIndex(ent, pEnt.getCurrentPathLength()-1);
						}
	
		                if (var1 != null)
		                {
		                	ent.getMoveHelper().setMoveTo(var1.xCoord, var1.yCoord, var1.zCoord, 0.53F);
		                	double dist = ent.getDistance(var1.xCoord, var1.yCoord, var1.zCoord);
		                	if (dist < 8) {
		                		//System.out.println("dist to node: " + dist);
		                		
		                	}
		                	if (dist < 2) {
		                		ent.getNavigator().getPath().incrementPathIndex();
		                	}
		                    //
		                }
					}
				}
				
				if (ent.isInWater()) {
					if (!ent.getNavigator().noPath()) {
						if (Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ) > 0.001F && Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ) < 0.5F) {
							//ent.moveFlying(ent.moveStrafing, ent.moveForward, 0.04F);
							//this.motionX *= 1.3F;
							//this.motionZ *= 1.3F;
						}
					}
					
				}
			//}
		}
	}
	
	public void updateAI() {
		
		if (useInv) entInv.updateTick();
		
		if (curCooldown_Melee > 0) { curCooldown_Melee--; }
        if (curCooldown_Ranged > 0) { curCooldown_Ranged--; }
        //if (curCooldown_FireGun > 0) { curCooldown_FireGun--; }
        //if (curCooldown_Reload > 0) { curCooldown_Reload--; }
        
		
		doMovement();
		
		if (entityToAttack != null && entityToAttack.isEntityAlive()) {
			float var2 = this.entityToAttack.getDistanceToEntity(ent);
            if (ent.canEntityBeSeen(this.entityToAttack))
            {
            	attackEntity(this.entityToAttack, var2);
            }
		} else {
			entityToAttack = null;
		}
		
		dangerLevel = 0;
		if (checkSurroundings() == 1) dangerLevel = 1;
		if (checkHealth()) dangerLevel = 2;
		jobMan.getPrimaryJob().checkHunger();
		
		
		
		//Safety overrides
		fleeing = false;
		//Safe
		if (dangerLevel == 0) {
			jobMan.tick();
			ent.moveSpeed = oldMoveSpeed;
			
		//Enemy detected? (by alert system?)
		} else if (dangerLevel == 1) {
			//no change for now
			jobMan.tick();
			ent.moveSpeed = oldMoveSpeed;
			
		//Low health, avoid death
		} else if (dangerLevel == 2) {
			
			//If nothing to avoid
			if (!jobMan.getPrimaryJob().avoid(true)) {
				fleeing = false;
				//no danger in area, try to continue job
				jobMan.tick();
				ent.moveSpeed = oldMoveSpeed;
			} else {
				fleeing = true;
				jobMan.getPrimaryJob().onLowHealth();
				
				//this.pathToEntity = this.getNavigator().getPath();
				
				//code to look ahead 1 node to speed up the pathfollow escape
				/*if (this.pathToEntity != null && this.pathToEntity.points != null) {
					int pIndex = this.pathToEntity.pathIndex+1;
					if (pIndex < this.pathToEntity.points.length) {
						if (this.worldObj.rayTraceBlocks(Vec3.createVectorHelper((double)pathToEntity.points[pIndex].xCoord + 0.5D, (double)pathToEntity.points[pIndex].yCoord + 1.5D, (double)pathToEntity.points[pIndex].zCoord + 0.5D), Vec3.createVectorHelper(posX, posY + (double)getEyeHeight(), posZ)) == null) {
							this.pathToEntity.pathIndex++;
						}
					}
				}*/
				
				
				
				ent.moveSpeed = fleeSpeed;
			}
		}
		
		if (currentAction == EnumActState.IDLE && jobMan.getPrimaryJob().state == EnumJobState.IDLE) {
			jobMan.getPrimaryJob().onIdleTick();
		} else if (currentAction == EnumActState.FIGHTING) {
			actFight();
		} else if (currentAction == EnumActState.WALKING) {
			actWalk();
		}
	}
	
	public void actFight() {
		//a range check maybe, but why, strafing/dodging techniques or something, lunging forward while using dagger etc...
		if (entityToAttack == null || entityToAttack.isDead) {
			entityToAttack = null;
			setState(EnumActState.IDLE);
		}
	}
	
	public void actWalk() {
		jobMan.getPrimaryJob().walkingTimeout--;
		//System.out.println(this.getDistance(targX, targY, targZ));
		if (ent.getDistance(targX, targY, targZ) < 2F || ent.getNavigator().getPath() == null || ent.getNavigator().getPath().isFinished()) {
			ent.getNavigator().clearPathEntity();
			setState(EnumActState.IDLE);
		} else if (jobMan.getPrimaryJob().walkingTimeout <= 0) {
			ent.getNavigator().clearPathEntity();
			setState(EnumActState.IDLE);
		}
	}
	
	public boolean checkHealth() {
		if (ent.getHealth() < ent.getMaxHealth() * 0.75) {
			return true;
		}
		return false;
	}
	
	public int checkSurroundings() {
		
		//fixEnemyTargetting();
		
		//if player or some hostile gets close, if not hunter perhaps run back to the village, find a hunter, update his job and have him get it
		if (checkAreaDelay < System.currentTimeMillis() && checkHealth()) {
			checkAreaDelay = System.currentTimeMillis() + 1500 + rand.nextInt(1000);
			
			float closest = 9999F;
			Entity clEnt = null;
			
			List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(checkRange, checkRange/2, checkRange));
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);
	            if(entInt.isEnemy(entity1))
	            {
	            	if ((ent).canEntityBeSeen(entity1)) {
	            		float dist = ent.getDistanceToEntity(entity1);
            			if (dist < closest) {
            				closest = dist;
            				clEnt = entity1;
            			}
	            		
	            		//break;
	            	}
	            }
	        }
	        
	        if (clEnt != null) { 
	        	//alertHunters(clEnt);
	        	return 1;
	        } else {
	        	return 0;
	        }
	        
	        
		} else {
			return -1;
		}
		
		
		
		//if threat - setstate moving -> village
	}
	
	protected void attackEntity(Entity var1, float var2) {
		if (useInv) entInv.sync();
    	
    	if (var2 < maxReach_Melee && var1.boundingBox.maxY > ent.boundingBox.minY && var1.boundingBox.minY < ent.boundingBox.maxY) {
    		if (curCooldown_Melee <= 0) {
    			ent.faceEntity(var1, 180, 180);
    			if (useInv) {
    				entInv.attackMelee(var1, var2);
    			} else {
    				entInt.attackMelee(var1, var2);
    			}
        		this.curCooldown_Melee = entInt.getCooldownMelee();
        	}
    	} else if (var2 < maxReach_Ranged) {
    		if (curCooldown_Ranged <= 0) {
    			ent.faceEntity(var1, 180, 180);
    			if (useInv) {
    				entInv.attackRanged(var1, var2);
    			} else {
    				entInt.attackRanged(var1, var2);
    			}
        		this.curCooldown_Ranged = entInt.getCooldownRanged();
    		}
    	}
    }
	
    public void updateWanderPath()
    {
        boolean flag = false;
        int i = -1;
        int j = -1;
        int k = -1;
        float f = -99999F;
        for (int l = 0; l < 10; l++)
        {
            int i1 = MathHelper.floor_double((ent.posX + (double)rand.nextInt(13)) - 6D);
            int j1 = MathHelper.floor_double((ent.posY + (double)rand.nextInt(7)) - 3D);
            int k1 = MathHelper.floor_double((ent.posZ + (double)rand.nextInt(13)) - 6D);
            float f1 = 1F;//getBlockPathWeight(i1, j1, k1);
            if (f1 > f)
            {
                f = f1;
                i = i1;
                j = j1;
                k = k1;
                flag = true;
            }
        }

        if (flag)
        {
        	walkTo(ent, i, j, k, this.maxPFRange, 600);
        }
    }
	
	public void checkPathfindLock() {
		if (waitingForNewPath) {
			ent.getNavigator().setPath(pathToEntity, c_CoroAIUtil.getMoveSpeed(ent));
			waitingForNewPath = false;
		}
	}
	
	public void setPathToEntity(PathEntity pathentity)
    {
        pathToEntity = pathentity;
        waitingForNewPath = true;
    }
	
	public void walkTo(Entity var1, int x, int y, int z, float var2, int timeout) {
		walkTo(var1, x, y, z, var2, timeout, 0);
	}
	
	public void walkTo(Entity var1, int x, int y, int z, float var2, int timeout, int priority) {
		PFQueue.getPath(ent, x, y, z, maxPFRange, priority);
		setState(EnumActState.WALKING);
		jobMan.getPrimaryJob().walkingTimeout = timeout;
		targX = x;
		targY = y;
		targZ = z;
	}
	
	public void huntTarget(Entity parEnt, int pri) {
		PFQueue.getPath(ent, parEnt, maxPFRange, pri);
		//System.out.println("huntTarget call: " + ent);
		this.entityToAttack = parEnt;
		setState(EnumActState.FIGHTING);
	}
	
	public void huntTarget(Entity parEnt) {
		huntTarget(parEnt, 0);
	}
}
