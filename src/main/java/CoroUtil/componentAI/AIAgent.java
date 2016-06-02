package CoroUtil.componentAI;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import CoroUtil.OldUtil;
import CoroUtil.componentAI.jobSystem.JobManager;
import CoroUtil.diplomacy.TeamInstance;
import CoroUtil.diplomacy.TeamTypes;
import CoroUtil.entity.EnumActState;
import CoroUtil.entity.EnumJobState;
import CoroUtil.formation.Formation;
import CoroUtil.inventory.AIInventory;
import CoroUtil.pathfinding.PFQueue;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.CoroUtilNBT;
import CoroUtil.util.Vec3;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.location.ISimulationTickable;

public class AIAgent {

	//Main
	public EntityLiving ent;
	public ICoroAI entInt;
	public AIInventory entInv;
	public boolean useInv = false;
	public JobManager jobMan;
	public Formation activeFormation;
	
	//Path
	public boolean pathAvailable; //marks that the callback fired
	public boolean pathRequested; //marks that the ai is already waiting for a path (prevents redundant path requests flooding up the queue)
	public Path pathToEntity;
	
	// AI fields \\
	
	//Attack
	public Entity entityToAttack;
	public Entity retaliateEntity;
	public boolean retaliateEnable = true;
	public int retaliateTicks = 0;
	public int retaliateTicksMax = 400;
	//public int cooldown_Melee;
	//public int cooldown_Ranged;
	public int curCooldown_Melee;
	public int curCooldown_Ranged;
	public float maxReach_Melee = 1.8F;
	public float maxReach_Ranged = 10;
	public boolean rangedInUse = false;
	public boolean rangedAimWhileInUse = false;
	public boolean meleeOverridesRangedInUse = true;
	public boolean meleeUseRightClick = false;
	public boolean canJoinFormations = false; //JobFormation sets this to true on its init when its used in the job list
	
	//Area scanning
	public long checkAreaDelay;
	public long checkRange = 16;
	public int dangerLevel = 0;
	public boolean fleeing = false;
	public Entity lastFleeEnt;
	public Vec3 lastSafeSpot;
	public boolean wasInLava = false;
	
	//Proximity based vanilla AI enhancing
	public boolean enhanceAIEnemies = false;
	public int enhancedAIDelay = 100;
	
	//Other fields
	public int maxPFRange = 32;
	public int PFRangeClose = 22;
	public int PFRangeFormation = 22;
	public int PFRangePathing = 32;
	
	public float collideResistClose = 0.2F;
	public float collideResistFormation = 0.5F;
	public float collideResistPathing = 0.8F;
	
	public Random rand;
	public int openedChest = 0;
	public float oldMoveSpeed;
	private float fleeSpeed = 0.33F; //set via setter that also sets attribute modifier now
	private float moveSpeed = 0.28F;
	public float lungeFactor = 1.0F;
	public int entID = -1; //created with random number generator range 999999999, overlap is possible
	public EnumActState currentAction;
	public boolean shouldFixBadYPathing = true;
	public boolean shouldPathfollow = true;
	public boolean shouldHeal = true;
	public int curCooldown_Heal;
	public int cooldown_Heal = 60;
	
	public int noMoveTicks = 0;
	public Vec3 prevPos = null;
	public int moveLeadTicks = 10;
	public float moveLeadFactorDist = 2F;
	public boolean shouldAvoid = true;
	public boolean useCustomMovement = false;
	
	//fields that should be moved to jobs?
	public int homeX;
	public int homeY = -1;
	public int homeZ;
	public boolean scanForHomeChest = false;
	public int targX;
	public int targY;
	public int targZ;
	public double maxDistanceFromHome = 96;
	public boolean facingWater = false;
	public boolean wasInWater = false;
	
	//x y z coords to link to a ManagedLocation for CoroUtil WorldDirector
  	public BlockCoord coordsManagedLocation;
  	public int locationMemberID = -1;
	
	//Diplomatic fields
	public boolean dipl_hostilePlayer = false;
	public TeamInstance dipl_info = TeamTypes.getType("neutral");
	public int dipl_spreadInfoDelay = 0;
	
	//NBT, Init and FakePlayer init helping fields
	public boolean hasBeenSpawnedOrNBTInitialized = false; //true once first time spawn or nbt is loaded
	public boolean waitingToMakeFakePlayer = false; //true once above is true, until successfull initialization of fake player is had
	
	public int lastMovementState = -1;
	
	//new 1.6.2 stuff - 0.45 values here are pointless and overridden
	public static final UUID uuid = UUID.randomUUID();
	public static AttributeModifier speedBoostFlee = (new AttributeModifier(uuid, "Speed boost flee", 0.45D, 0)).setSaved(false);
	public static AttributeModifier speedBoostAttack = (new AttributeModifier(uuid, "Speed boost attack", 0.45D, 0)).setSaved(false);
	
	//arm swingin
	public boolean swingArm;
    public int swingTick;
    
    //moved v5 despawning to v4
	private int tickAge;
	private int tickDespawn;
	
	public AIAgent(ICoroAI parEnt, boolean useInventory) {
		ent = (EntityLiving)parEnt;
		entInt = parEnt;
		useInv = useInventory;
		/*if (useInv) {
			entInv = new AIFakePlayer(this);
		}*/
		entInv = new AIInventory(ent);
		jobMan = new JobManager(this);
		rand = new Random();
		setState(EnumActState.IDLE);

		if (entID == -1) entID = rand.nextInt(999999999);
	}
	
	/*public void setSpeedMove(float speed) {
		fleeSpeed = speed;
	}*/
	
	public void applyEntityAttributes() {
		//baseline movespeed
		ent.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(moveSpeed);
	}
	
	public void attrRemoveSpeeds() {
		IAttributeInstance attributeinstance = ent.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        attributeinstance.removeModifier(speedBoostAttack);
        attributeinstance.removeModifier(speedBoostFlee);
	}

	public void attrSetSpeedFlee() {
		attrRemoveSpeeds();
		IAttributeInstance attributeinstance = ent.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
		attributeinstance.applyModifier(speedBoostFlee);
	}
	
	public void attrSetSpeedAttack() {
		attrRemoveSpeeds();
		IAttributeInstance attributeinstance = ent.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
		attributeinstance.applyModifier(speedBoostAttack);
	}
	
	public void attrSetSpeedNormal() {
		attrRemoveSpeeds();
	}
	
	public void setSpeedFleeAdditive(float speed) {
		fleeSpeed = speed;
		speedBoostFlee = (new AttributeModifier(uuid, "Speed boost flee", fleeSpeed, 0)).setSaved(false);
	}
	
	//if needed
	public void setSpeedAttackAdditive(float speed) {
		//fleeSpeed = speed;
		speedBoostAttack = (new AttributeModifier(uuid, "Speed boost attack", speed, 0)).setSaved(false);
	}
	
	public void setSpeedNormalBase(float var) {
		moveSpeed = var;
		
		//System.out.println("temp disable");
		//c_CoroAIUtil.setMoveSpeed(ent, var);
		//oldMoveSpeed = var;
		/*if (!ent.worldObj.isRemote) {
			this.dataWatcher.updateObject(23, Integer.valueOf((int)(var * 1000)));
		}*/
	}
	
	public void setTeam(String parTeam) {
		dipl_info = TeamTypes.getType(parTeam);
	}
	
	public void spawnedOrNBTReloadedInit() {
		dbg(ent.getEntityId() + " - CALLED: spawnedOrNBTReloadedInit()"); 
		hasBeenSpawnedOrNBTInitialized = true;
		/*if (useInv) {
			waitingToMakeFakePlayer = true;
		} else {*/
			postFullInit();
		//}
	}
	
	public void postFullInit() {
		if (homeY == -1) {
			homeX = (int)Math.floor(ent.posX);
			homeY = (int)Math.floor(ent.posY);
			homeZ = (int)Math.floor(ent.posZ);
		}
		
		//by this point ManagedLocations SHOULD be loaded via first firing WorldLoad event, no race condition issues should exist
		//TODO: readd 1.8.8
		/*ManagedLocation ml = getManagedLocation();
		if (ml != null) {
			//unitType is mostly unused atm
			ml.addEntity("member", ent);
		} else {
			//this should be expected, remove this sysout once you are sure this only happens at expected times
			//removing without being sure, months in between development
			//CoroAI.dbg("AIAgent Entitys home has been destroyed or never had one set!");
		}*/
	}
	
	//TODO: readd 1.8.8
	/*public ManagedLocation getManagedLocation() {
		if (coordsManagedLocation != null) {
			WorldDirector wd = WorldDirectorManager.instance().getCoroUtilWorldDirector(ent.worldObj);
			ISimulationTickable ml = wd.getTickingSimluationByLocation(coordsManagedLocation);
			if (ml instanceof ManagedLocation) {
				return (ManagedLocation) ml;
			}
		}
		return null;
	}
	
	public void setManagedLocation(BlockCoord parLocation) {
		coordsManagedLocation = parLocation;
	}
	
	public void setManagedLocation(ManagedLocation parLocation) {
		coordsManagedLocation = parLocation.spawn;
	}*/
	
	public void setLocationMemberID(int parID) {
		locationMemberID = parID;
	}
	
	public boolean notPathing() {
		if (!pathRequested && this.ent.getNavigator().noPath()) return true;
		return false;
	}
	
	public void initJobs() {
		//if (useInv) entInv.initJobs();
	}
	
	public void setState(EnumActState eka) {
		currentAction = eka;
	}
	
	public void entityInit()
    {
		//IDS USED ELSEWHERE:
		//27 is used in baseentai
		//28 is used in HungryZombie size setting
		
        //this.dataWatcher.addObject(20, Integer.valueOf(0)); //Move speed state
        //this.dataWatcher.addObject(21, Integer.valueOf(0)); //Swing arm state
		
        ent.getDataWatcher().addObject(22, Integer.valueOf(0)); //onGround state for fall through floor fix
        //ent.getDataWatcher().addObject(23, new Integer(ent.getMaxHealth()));
        ent.getDataWatcher().addObject(24, Integer.valueOf(0)); //AI state, used for stuff like sitting animation, etc
        ent.getDataWatcher().addObject(25, Integer.valueOf(0)); //swing arm state
        
    }
	
	public int getDWonGround() {
		return ent.getDataWatcher().getWatchableObjectInt(22);
	}
	
	public EnumActState getDWStateAI() {
		return EnumActState.get(ent.getDataWatcher().getWatchableObjectInt(24));
	}
	
	public void swingArm() {
		swingArm = true;
	}
	
	public void onLivingUpdateTick() {
		if (ent.worldObj.isRemote) {
			if (ent.getDataWatcher().getWatchableObjectInt(25) == 1) {
				swingArm = true;
			}
			if (ent.getDataWatcher().getWatchableObjectInt(22) == 1) {
				ent.motionY = 0F;
				ent.onGround = true;
			} else {
				ent.motionY = 0F;
				ent.onGround = false;
			}
			//ent.health = ent.getDataWatcher().getWatchableObjectInt(23);
		} else {
			ent.getDataWatcher().updateObject(22, Integer.valueOf(ent.onGround ? 1 : 0));
			//ent.getDataWatcher().updateObject(23, Integer.valueOf(ent.health));
			ent.getDataWatcher().updateObject(24, Integer.valueOf(this.currentAction.ordinal()));
			ent.getDataWatcher().updateObject(25, Integer.valueOf(swingArm ? 1 : 0));
			if (swingArm) swingArm = false; //to reset the state so client doesnt get extra swing arm state?
		}
		//if (useInv) entInv.onLivingUpdateTick();
	}
	
	public void updateAITasks() {
		//System.out.println("AIAgent inc age and despawn calls missing");
		/*OldUtil.addAge(ent, 1);
		OldUtil.despawnEntity(ent);*/
        
        //if (fakePlayer == null) return;
        
        //this.func_48090_aM().func_48481_a();
        //this.targetTasks.onUpdateTasks();
        //this.tasks.onUpdateTasks();
		lastMovementState = -1;
        
		//if (useInv) entInv.updateTick();
		
        if (jobMan.getPrimaryJob() == null) return;
        
        if (dangerLevel != 2 && jobMan.getPrimaryJob().shouldTickCloseCombat()) {
        	lastMovementState = 0;
        	maxPFRange = PFRangeClose;
        	ent.entityCollisionReduction = collideResistClose;
        	jobMan.getPrimaryJob().onTickCloseCombat();
        } else if (dangerLevel != 2 && jobMan.getPrimaryJob().shouldTickFormation() && activeFormation.leader != entInt && !((EntityLivingBase)activeFormation.leader).isInWater()) {
        	lastMovementState = 1;
        	maxPFRange = PFRangeFormation;
        	ent.entityCollisionReduction = collideResistFormation;
        	jobMan.getPrimaryJob().onTickFormation();
        } else {
        	lastMovementState = 2;
        	maxPFRange = PFRangePathing;
        	ent.entityCollisionReduction = collideResistPathing;
        	if (shouldPathfollow) {
        		checkPathfindLock();
        		ent.getNavigator().onUpdateNavigation();
        		
        	}
        }
        
        /*if (ent instanceof EntityKoaShaman) {
        	System.out.println(ent.entityId + " state " + lastMovementState);
        }*/
        //
        
        if (lastMovementState != 1) tickPathFollowHelp();
        
        tickMovementHelp();
        
        updateAI();
        tickAgeEntity();
        //this.func_48097_s_();
        if (useCustomMovement) {
        	jobMan.priJob.tickCustomMovement();
        } else {
        	ent.getMoveHelper().onUpdateMoveHelper();
	        ent.getLookHelper().onUpdateLook();
	        ent.getJumpHelper().doJump();
        }
	}
	
	public void tickAgeEntity()
    {

    	tickAge++;

    	if (!ent.isNoDespawnRequired()) {

			//reset respawn chance if pathing, otherwise tick
			if (ent.getNavigator().noPath() || entityToAttack == null) {
    			tickDespawn++;
    		} else {
    			tickDespawn = 0;
    		}

    		if (ent.worldObj.getTotalWorldTime() % 20 == 0) {
    			EntityPlayer entityplayer = ent.worldObj.getClosestPlayerToEntity(ent, -1.0D);

    			if (entityplayer != null)
    			{
    				double d0 = entityplayer.posX - ent.posX;
    				double d1 = entityplayer.posY - ent.posY;
    				double d2 = entityplayer.posZ - ent.posZ;
    				double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

    				Random rand = new Random();

					boolean despawn = false;

    				if (/*this.canDespawn() && */d3 > 128/*128*/)
    				{
    					//System.out.println("despawned a");
    					despawn = true;
    				}

    				if (tickDespawn > 600) {
	    				if (d3 > 32/* && this.canDespawn()*/)
	    				{
	    					//System.out.println("despawned b");
	    					despawn = true;
	    				}
	    				else if (d3 < 128)
	    				{
	    					tickDespawn = 0;
	    				}
    				}
    				
    				if (despawn) {
    					//if (blackboard.getTarget() == null) {
    						ent.setDead();
    					//}
    				}
    			}
    		}
    	} else {
    		tickDespawn = 0;
    	}
    }
	
	public void tickPathFollowHelp() {
		
		//this was breaking formations or something, see if we can live without it now?
		//- moved to its own method, only run when path following, might help? might bring back endless water loop, find out
		if (ent.isInWater()) {
				
				//pathfollow fix
				if (true) {
					if (ent.getNavigator().getPath() != null) {
						Path pEnt = ent.getNavigator().getPath();
						int index = pEnt.getCurrentPathIndex()+1;
						//index--;
						if (index < 0) index = 0;
						if (index > pEnt.getCurrentPathLength()) index = pEnt.getCurrentPathLength()-1;
						Vec3d var1 = null;
						try {
							var1 = pEnt.getVectorFromIndex(ent, pEnt.getCurrentPathIndex());
						} catch (Exception ex) {
							var1 = pEnt.getVectorFromIndex(ent, pEnt.getCurrentPathLength()-1);
						}
	
		                if (var1 != null)
		                {
		                	ent.getMoveHelper().setMoveTo(var1.xCoord, var1.yCoord, var1.zCoord, 0.53F);
		                	double dist = ent.getDistance(var1.xCoord, var1.yCoord, var1.zCoord);
		                	if (dist < 3) {
		                		ent.getNavigator().getPath().incrementPathIndex();
		                	}
		                }
					}
				}
				
				if (!ent.getNavigator().noPath()) {
					
				}
		}
		
		//No movement on x z
		if (!ent.getNavigator().noPath()) {
			Vec3 curPos = new Vec3(ent.posX, ent.posY, ent.posZ);
			if (prevPos == null) {
				prevPos = curPos;
			} else {
				//make y not count
				prevPos = new Vec3(prevPos.xCoord, ent.posY, prevPos.zCoord);
			}
			
			if (curPos.distanceTo(prevPos) < 0.01) {
				noMoveTicks++;
			} else {
				noMoveTicks = 0;
			}
			
			if (noMoveTicks > 60) {
				//System.out.println("noMoveTicks path reset!");
				ent.getNavigator().clearPathEntity();
				noMoveTicks = 0;
			}
		
			prevPos = curPos;
		}
		
	}
	
	public void tickMovementHelp() {

		if (ent.isInWater()) {
			ent.motionY += 0.03D;
			
			if (Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ) > 0.001F && Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ) < 0.2F) {
				//ent.moveFlying(ent.moveStrafing, ent.moveForward, 0.04F);
				ent.motionX *= 1.2F;
				ent.motionZ *= 1.2F;
			}
			
		} else if (ent.isNotColliding()) {
			ent.motionY += 0.07D;
			float speed = 1.2F;
			if (lastSafeSpot != null) {
				ent.getMoveHelper().setMoveTo(lastSafeSpot.xCoord, lastSafeSpot.yCoord, lastSafeSpot.zCoord, speed);
			} else {
				if (jobMan.getPrimaryJob().tamable.isTame() && jobMan.getPrimaryJob().tamable.ownerCachedInstance != null) {
					ent.getMoveHelper().setMoveTo(jobMan.getPrimaryJob().tamable.ownerCachedInstance.posX, jobMan.getPrimaryJob().tamable.ownerCachedInstance.posY, jobMan.getPrimaryJob().tamable.ownerCachedInstance.posZ, speed);
				}
			}
			wasInLava = true;
		}
		
		if (wasInLava && !ent.isNotColliding()) {
			wasInLava = false;
			ent.extinguish();
		}
		
		//if (true) return;
		//fix for last node being too high
		if (!ent.worldObj.isRemote) {
			Path pe = ent.getNavigator().getPath();
			if (pe != null) {
				if (pe.getCurrentPathLength() == 1) {
					//if (job.priJob == EnumJob.TRADING) {
						if (pe.getFinalPathPoint().yCoord - ent.posY > 0.01F) {
							//System.out.println("tickMovementHelp:" + (pe.getFinalPathPoint().yCoord - ent.posY));
							ent.getNavigator().clearPathEntity();
						}
					//}
				}
			}
		}
		
		if (ent.onGround && ent.getNavigator().noPath()) lastSafeSpot = new Vec3(ent.posX, ent.posY, ent.posZ);
		
		if (shouldFixBadYPathing) fixBadYPathing();
	}
	
	public void fixBadYPathing() {
		
		if (ent.getNavigator().getPath() != null) {
			Path pEnt = ent.getNavigator().getPath();
			int index = pEnt.getCurrentPathIndex();
			//index--;
			if (index < 0) index = 0;
			if (index >= pEnt.getCurrentPathLength()) index = pEnt.getCurrentPathLength()-1;
			Vec3d var1 = null;
			try {
				var1 = pEnt.getVectorFromIndex(ent, index);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("errrrrrrrrrrrrrrr");
				var1 = pEnt.getVectorFromIndex(ent, pEnt.getCurrentPathLength()-1);
			}

            if (var1 != null)
            {
            	//fast water pathing
            	//this.getMoveHelper().setMoveTo(var1.xCoord, var1.yCoord, var1.zCoord, 0.53F);
            	double dist = ent.getDistance(var1.xCoord, ent.getEntityBoundingBox().minY, var1.zCoord);
            	//System.out.println(dist);
            	if (dist <= 0.5F) {
            		ent.getNavigator().getPath().incrementPathIndex();
            	}
            }
		}
	}
	
	public void updateHealing() {
		if (curCooldown_Heal > 0) curCooldown_Heal--;
		if (curCooldown_Heal <= 0) {
			curCooldown_Heal = cooldown_Heal;
			heal(1);
		}
	}
	
	public void heal(int val) {
		ent.heal(val);
		//dbg("healing, new health: " + ent.getHealth());
		//if (useInv) entInv.sync();
	}
	
	public void updateAI() {
		
		
		
		if (jobMan.getPrimaryJob() == null) return;
		
		if (curCooldown_Melee > 0) curCooldown_Melee--;
        if (curCooldown_Ranged > 0) curCooldown_Ranged--;
        
        //if (curCooldown_FireGun > 0) { curCooldown_FireGun--; }
        //if (curCooldown_Reload > 0) { curCooldown_Reload--; }
		
        jobMan.getPrimaryJob().onTickChestScan();
        
        //Formation cleanup
        if (activeFormation != null && activeFormation.listEntities.size() == 0) {
        	activeFormation = null;
        }
        
        //Remove retaliate target on tick 0 if it is not a natural enemy
        if (retaliateTicks > 0) {
        	retaliateTicks--;
        	if (retaliateTicks == 0 && retaliateEntity != null) {
        		if (retaliateEntity == entityToAttack && !entInt.isEnemy(retaliateEntity)) {
        			entityToAttack = null;
        			retaliateEntity = null;
        		}
        	}
        }
        
        if (entityToAttack != null && entityToAttack.isEntityAlive()) {
			float var2 = this.entityToAttack.getDistanceToEntity(ent);
            if (ent.canEntityBeSeen(this.entityToAttack))
            {
            	attackEntity(this.entityToAttack, var2);            	
            }
            
            /*if (rangedInUse) {
    			if (dangerLevel == 0 && lastMovementState == 0 && rangedAimWhileInUse) {
    				ent.faceEntity(entityToAttack, 180, 180);
    			}
    			if (useInv) {
    				entInv.rangedUsageUpdate(entityToAttack, var2);
    			} else {
    				rangedUsageUpdate(entityToAttack, var2);
    			}
    		}*/
            
		} else {
			entityToAttack = null;
			//if (rangedInUse) rangedUsageCancelCharge();
		}
		
		if (shouldHeal) updateHealing();
		
		dangerLevel = 0;
		if (checkSurroundings() == 1) dangerLevel = 1;
		//if (checkHealth()) dangerLevel = 2;
		if (jobMan.getPrimaryJob().checkDangers()) dangerLevel = 2;
		jobMan.getPrimaryJob().checkHunger();
		
		//Safety overrides
		fleeing = false;
		//Safe
		if (dangerLevel == 0) {
			jobMan.tick();
			attrSetSpeedNormal();
			//c_CoroAIUtil.setMoveSpeed(ent, oldMoveSpeed);
			
		//Enemy detected? (by alert system?)
		} else if (dangerLevel == 1) {
			//no change for now
			jobMan.tick();
			attrSetSpeedNormal();
			//c_CoroAIUtil.setMoveSpeed(ent, oldMoveSpeed);
			
		//Low health, avoid death
		} else if (dangerLevel == 2) {
			
			//If nothing to avoid
			if (!jobMan.getPrimaryJob().avoid(true) || !shouldAvoid) {
				fleeing = false;
				//no danger in area, try to continue job
				jobMan.tick();
				attrSetSpeedNormal();
				//c_CoroAIUtil.setMoveSpeed(ent, oldMoveSpeed);
			} else {
				fleeing = true;
				jobMan.getPrimaryJob().onLowHealth();
				attrSetSpeedFlee();
				//c_CoroAIUtil.setMoveSpeed(ent, Math.max(oldMoveSpeed, fleeSpeed));
			}
		}
		
		if (currentAction == EnumActState.IDLE && jobMan.getPrimaryJob().state == EnumJobState.IDLE) {
			jobMan.getPrimaryJob().onIdleTick();
		} else if (currentAction == EnumActState.FIGHTING) {
			actFight();
		} else if (currentAction == EnumActState.WALKING) {
			actWalk();
		}
		
		entInv.tick();
	}
	
	public void actFight() {
		//a range check maybe, but why, strafing/dodging techniques or something, lunging forward while using dagger etc...
		if (entityToAttack == null || entityToAttack.isDead || entityToAttack == ent || (entityToAttack instanceof EntityLivingBase && ((EntityLivingBase)entityToAttack).deathTime > 0)) {
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
			
			List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.getEntityBoundingBox().expand(checkRange, checkRange/2, checkRange));
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);
	            if(jobMan.getPrimaryJob().isEnemy(entity1))
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
	
	public void faceEntity(Entity par1Entity, float par2, float par3)
    {
        double d0 = par1Entity.posX - ent.posX;
        double d1 = par1Entity.posZ - ent.posZ;
        double d2;

        if (par1Entity instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase)par1Entity;
            d2 = entitylivingbase.posY + (double)entitylivingbase.getEyeHeight() - (ent.posY + (double)ent.getEyeHeight());
        }
        else
        {
            d2 = (par1Entity.getEntityBoundingBox().minY + par1Entity.getEntityBoundingBox().maxY) / 2.0D - (ent.posY + (double)ent.getEyeHeight());
        }

        double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d1 * d1);
        float f2 = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
        float f3 = (float)(-(Math.atan2(d2, d3) * 180.0D / Math.PI));
        ent.rotationPitch = this.updateRotation(ent.rotationPitch, f3, par3);
        ent.rotationYaw = this.updateRotation(ent.rotationYaw, f2, par2);
    }
	
	protected void attackEntity(Entity var1, float var2) {
		/*if (useInv) {
			//Cancel if outsourced management says no
			if (entInv.inventoryOutsourced != null && !entInv.inventoryOutsourced.canAttack()) return;
			entInv.sync();
		}*/
    	
		//no charge fix
		//if (useInv && entInv.rangedInUseTicksMax == 0) rangedInUse = false;
		
		float prevRotYaw = ent.rotationYaw;
		float prevRotPitch = ent.rotationPitch;
		
    	if ((!rangedInUse || meleeOverridesRangedInUse) && var2 < maxReach_Melee && var1.getEntityBoundingBox().maxY > ent.getEntityBoundingBox().minY && var1.getEntityBoundingBox().minY < ent.getEntityBoundingBox().maxY) {
    		if (curCooldown_Melee <= 0) {
    			if (rangedInUse) rangedUsageCancelCharge();
    			
    			ent.faceEntity(var1, 180, 180);
    			if (!meleeUseRightClick) {
	    			if (useInv) {
	    				entInv.attackMelee(var1, var2);
	    			} else {
	    				entInt.attackMelee(var1, var2);
	    			}
    			} else {
    				if (useInv) {
        				//entInv.attackRanged(var1, var2);
    					if (entInv.inventory == null) return;
    					faceEntity(ent, 180, 180);
    					entInv.setSlotActive(entInv.slot_Melee);
    					entInv.performRightClick();
    					//entInv.setCurrentSlot(entInv.slot_Melee);
    					////this.setCurrentSlot(entInv.slot_Ranged);
    					//entInv.rightClickItem();
        			} else {
        				entInt.attackRanged(var1, var2);
        			}
    			}
        		this.curCooldown_Melee = entInt.getCooldownMelee();
        	}
    	} else if (rangedInUse) {
    		//updates elsewhere so doesnt need this method to get called (DIRTY LIES)
    	} else if (var2 < maxReach_Ranged) {
    		if (curCooldown_Ranged <= 0 && curCooldown_Melee < maxReach_Melee - (maxReach_Melee / 4)) {
    			ent.faceEntity(var1, 180, 180);
    			if (useInv) {
    				entInv.attackRanged(var1, var2);
    			} else {
    				entInt.attackRanged(var1, var2);
    			}
        		this.curCooldown_Ranged = entInt.getCooldownRanged(); //keep here for fallback when charging items not used
    		}
    	}
    	
    	//prevent locking in on target
    	ent.rotationYaw = prevRotYaw;
    	ent.rotationPitch = prevRotPitch;
    }
	
	//placeholder
	public void rangedUsageUpdate(Entity ent, float dist) {
		rangedUsageCancelCharge();
	}
	
	public void rangedUsageStartCharge() {
		rangedInUse = true;
	}
	
	public void rangedUsageCancelCharge() {
		//dbg(ent.entityId + " - rangedUsageCancelCharge() stopped using item!");
		rangedInUse = false;
		this.curCooldown_Ranged = entInt.getCooldownRanged();
		//if (useInv) entInv.rangedUsageCancelCharge();
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
		if (pathAvailable) {
			//ent.getMoveHelper().setMoveTo(0, 0, 0, 0);
			ent.getNavigator().clearPathEntity();
			//System.out.println(ent);
			ent.getNavigator().setPath(pathToEntity, OldUtil.getMoveSpeed(ent));
			pathAvailable = false;
			pathRequested = false;
		}
	}
	
	public void setPathToEntity(Path pathentity)
    {
		jobMan.getPrimaryJob().setPathToEntity(pathentity);
    }
	
	public void setPathToEntityForce(Path pathentity)
    {
		//System.out.println("force set path");
        pathToEntity = pathentity;
        pathAvailable = true;
    }
	
	public void walkTo(Entity var1, BlockCoord coords, float var2, int timeout) {
		walkTo(var1, coords.posX, coords.posY, coords.posZ, var2, timeout, 0);
	}
	
	public void walkTo(Entity var1, int x, int y, int z, float var2, int timeout) {
		walkTo(var1, x, y, z, var2, timeout, 0);
	}
	
	public void walkTo(Entity var1, int x, int y, int z, float var2, int timeout, int priority) {
		pathRequested = true; //redundancy preventer
		PFQueue.getPath(ent, x, y, z, var2, priority);
		setState(EnumActState.WALKING);
		jobMan.getPrimaryJob().walkingTimeout = timeout;
		targX = x;
		targY = y;
		targZ = z;
	}
	
	public void walkToMark(Entity var1, Path pe, int timeout) {
		//PFQueue.getPath(ent, x, y, z, maxPFRange, priority);
		setState(EnumActState.WALKING);
		jobMan.getPrimaryJob().walkingTimeout = timeout;
		targX = pe.getFinalPathPoint().xCoord;
		targY = pe.getFinalPathPoint().yCoord;
		targZ = pe.getFinalPathPoint().zCoord;
	}
	
	public void walkToMark(Entity var1, BlockCoord coords, int timeout) {
		//PFQueue.getPath(ent, x, y, z, maxPFRange, priority);
		setState(EnumActState.WALKING);
		jobMan.getPrimaryJob().walkingTimeout = timeout;
		targX = coords.posX;
		targY = coords.posY;
		targZ = coords.posZ;
	}
	
	public void setTargetRetaliate(Entity targ) {
		setTarget(targ);
		retaliateEntity = targ;
		retaliateTicks = retaliateTicksMax;
	}
	
	public void setTarget(Entity parEnt) {
		this.entityToAttack = parEnt;
		if (jobMan.getPrimaryJob().isInFormation() && activeFormation.leaderTarget == null && parEnt instanceof EntityLivingBase) activeFormation.leaderTarget = (EntityLivingBase)parEnt;
		setState(EnumActState.FIGHTING);
	}
	
	public void huntTarget(Entity parEnt, int pri) {
		//if (ent.isInWater() || !jobMan.getPrimaryJob().isInFormation() || activeFormation.leader == entInt) {
		boolean isLeader = (jobMan.getPrimaryJob().isInFormation() && activeFormation.leader == entInt);
		//if (isLeader) System.out.println("@!@!@!@");
		if (lastMovementState == 2 || isLeader) {
			pathRequested = true; //redundancy preventer
			PFQueue.getPath(ent, parEnt, maxPFRange, pri);
		}
		
		//System.out.println("huntTarget call: " + ent);
		setTarget(parEnt);
	}
	
	public void huntTarget(Entity parEnt) {
		huntTarget(parEnt, 0);
	}
	
	public void moveTo(BlockCoord coords) {
		PFQueue.getPath(ent, coords.posX, coords.posY, coords.posZ, maxPFRange, 0);
		walkToMark(null, coords, 600);
	}
	
	public void faceCoord(BlockCoord coord, float f, float f1) {
		faceCoord(coord.posX, coord.posY, coord.posZ, f, f1);
	}
	
	public void faceCoord(int x, int y, int z, float f, float f1)
    {
        double d = x+0.5F - ent.posX;
        double d2 = z+0.5F - ent.posZ;
        double d1;
        d1 = y+0.5F - (ent.posY + (double)ent.getEyeHeight());
        
        double d3 = MathHelper.sqrt_double(d * d + d2 * d2);
        float f2 = (float)((Math.atan2(d2, d) * 180D) / 3.1415927410125732D) - 90F;
        float f3 = (float)(-((Math.atan2(d1, d3) * 180D) / 3.1415927410125732D));
        ent.rotationPitch = -updateRotation(ent.rotationPitch, f3, f1);
        ent.rotationYaw = updateRotation(ent.rotationYaw, f2, f);
    }
	
	public float updateRotation(float f, float f1, float f2)
    {
        float f3;
        for(f3 = f1 - f; f3 < -180F; f3 += 360F) { }
        for(; f3 >= 180F; f3 -= 360F) { }
        if(f3 > f2)
        {
            f3 = f2;
        }
        if(f3 < -f2)
        {
            f3 = -f2;
        }
        return f + f3;
    }
	
	public RayTraceResult rayTrace(double reachDist, float yOffset, Vec3 randLook)
    {
		float partialTick = 1F;
		
        Vec3d var4 = new Vec3d(ent.posX, ent.posY+yOffset, ent.posZ);
        Vec3d var5 = ent.getLook(partialTick);
        if (randLook != null) var5.addVector(randLook.xCoord, randLook.yCoord, randLook.zCoord);
        Vec3d var6 = var4.addVector(var5.xCoord * reachDist, var5.yCoord * reachDist, var5.zCoord * reachDist);
        return ent.worldObj.rayTraceBlocks(var4, var6);
    }
	
	public boolean isInFormation() {
		return jobMan.getPrimaryJob().isInFormation();
	}
	
	public void readEntityFromNBT(NBTTagCompound var1) {
		this.entInv.nbtRead(var1.getCompoundTag("inventory"));
		entID = var1.getInteger("ICoroAI_entID");
		locationMemberID = var1.getInteger("locationMemberID");
		homeX = var1.getInteger("homeX");
		homeY = var1.getInteger("homeY");
		homeZ = var1.getInteger("homeZ");
		String tameName = var1.getString("tamedByUser");
		if (!tameName.equals("")) {
			jobMan.getPrimaryJob().tamable.tameBy(tameName);
		}
		
		if (var1.hasKey("coordsManagedLocationX")) coordsManagedLocation = CoroUtilNBT.readCoords("coordsManagedLocation", var1);
		
		spawnedOrNBTReloadedInit();
	}
	
	public void writeEntityToNBT(NBTTagCompound var1) {
		var1.setTag("inventory", entInv.nbtWrite());
		var1.setInteger("ICoroAI_entID", entID);
		var1.setInteger("locationMemberID", locationMemberID);
		var1.setInteger("homeX", homeX);
		var1.setInteger("homeY", homeY);
		var1.setInteger("homeZ", homeZ);
		var1.setString("tamedByUser", jobMan.getPrimaryJob().tamable.owner);
		
		if (coordsManagedLocation != null) CoroUtilNBT.writeCoords("coordsManagedLocation", coordsManagedLocation, var1);
	}
	
	public boolean hookHit(DamageSource par1DamageSource, int par2) {
		if (!ent.worldObj.isRemote) {
			return jobMan.hookHit(par1DamageSource, par2);
		} else return true;
	}
	
	public boolean hookInteract(EntityPlayer par1EntityPlayer) {
		if (!ent.worldObj.isRemote) {
			return jobMan.hookInteract(par1EntityPlayer);
		} else return false;
	}
	
	public void hookSetDead() {
		if (!ent.worldObj.isRemote) {
			if (coordsManagedLocation != null) {
				WorldDirector wd = WorldDirectorManager.instance().getCoroUtilWorldDirector(ent.worldObj);
				ISimulationTickable ml = wd.getTickingSimluationByLocation(coordsManagedLocation);
				//TODO: readd 1.8.8
				/*if (ml != null && ml instanceof ManagedLocation) {
					((ManagedLocation) ml).hookEntityDied(ent);
				}*/
			}
		}
	}
	
	public boolean isThreat(Entity ent) {
		if (ent instanceof EntityCreeper || ent instanceof EntityEnderman) {
			return true;
		}
		return false;
	}
	
	public void dbg(Object obj) {
		//System.out.println(obj);
	}
	
	public void cleanup() {
		entInv.cleanup();
		//kill cyclical references
		//System.out.println("cleaning up entity " + ent.entityId);
		PFQueue.pfDelays.remove(ent);
		
		if (coordsManagedLocation != null) {
			WorldDirector wd = WorldDirectorManager.instance().getCoroUtilWorldDirector(ent.worldObj);
			ISimulationTickable ml = wd.getTickingSimluationByLocation(coordsManagedLocation);
			//TODO: readd 1.8.8
			/*if (ml != null && ml instanceof ManagedLocation) {
				((ManagedLocation) ml).hookEntityDestroyed(ent);
			}*/
		}
		
		jobMan.cleanup();
		ent = null;
		entInt.cleanup();
		entInt = null;
		entInv = null;
		jobMan = null;
		activeFormation = null;
		retaliateEntity = null;
		entityToAttack = null;
	}
}
