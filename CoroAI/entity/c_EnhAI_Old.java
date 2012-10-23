package CoroAI.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import CoroAI.*;
import CoroAI.entity.*;

import net.minecraft.src.*;

public class c_EnhAI_Old extends c_PlayerProxy implements c_IEnhAI
{
	public boolean debug = false;
	
	public boolean gender;	//true for male, false for female
	public EnumActState currentAction;
	public String name;
	
	//used for linking names to meanings, koa only thing, move to new separation class 
    public int nameIndex;
    
    public Random rand;
    
    //broken job safety timeouts, etc
    //public int fishingTimeout;
    
	//public int walkingTimeout;
	public int jobTimeout;
	public int openedChest = 0;
	
	//Diplomatic fields
	public boolean dipl_hostilePlayer = false;
	public EnumTeam dipl_team = EnumTeam.HOSTILES;
	public int dipl_spreadInfoDelay = 0;
		
	// JOB FIELDS \\
	
	//Hunting fields
	
	
	//Fishing fields
	
	
	//Trading fields
	
	
	// JOB FIELDS //
	
	//walking target
	public int targX;
	public int targY;
	public int targZ;
	
	//follow target
	public Entity followTarget;
	
	//Village fields
	public int homeX;
	public int homeY;
	public int homeZ;
	public double maxDistanceFromHome = 96;
	
	public long checkAreaDelay;
	public long checkRange = 16;
	
	//public TCConversationHandler conversationHandler;

	//public EnumKoaOccupation occupation;
	//public EnumKoaOccupation primaryOccupation;
    //public EnumKoaOccupationState occupationState;
    
    //NEW REFACTORED JOB CLASS
    public JobManager job;
    
    //priority stuff?
    public boolean occupationReady = true;
    public int dangerLevel = 0;
	
	//for debugging
    public String oldName;
	
	public boolean facingWater = false;
	public boolean wasInWater = false;
	public float oldMoveSpeed;
	
	public int enhancedAIDelay = 100;
	
	//flee based stuff
	public int prevKoaHealth;
	public Entity lastFleeEnt;
	public boolean tryingToFlee;
	
	public c_EnhAI_Old(World world) 
	{
		super(world);		
		
		//new
		//this.job = new JobManager(this);
		
		this.rand = new Random();
		
		initJobAndStates(EnumJob.UNEMPLOYED, false);
		
		homeX = 0;
		name = ".";
		
		/*try {
			homeX = (int)ModLoader.getMinecraftInstance().thePlayer.posX;
			homeY = (int)ModLoader.getMinecraftInstance().thePlayer.posY;
			homeZ = (int)ModLoader.getMinecraftInstance().thePlayer.posZ;
		} catch (Exception ex) { homeX = 0; }*/

		oldMoveSpeed = moveSpeed;
		
	//	name = getName(gender);
	}
	
	public float getMoveSpeed() {
		return moveSpeed;
	}
	
	public void initJobAndStates(EnumJob job) {
		initJobAndStates(job, true);
	}
	
	public void initJobAndStates(EnumJob job, boolean initItems) {
		this.job.setPrimaryJob(job);
		//swapJob(job);
		setState(EnumActState.IDLE);
		//setJobState(EnumJobState.IDLE);
		
		//NEW!
		this.job.swapJob(EnumJob.HUNTER);
		
		if (initItems) {
			setOccupationItems();
		}
		
	}
	
	
	
	public void swapJob(EnumJob job) {
		//occupation = job;
		setState(EnumActState.IDLE);
		//setJobState(EnumJobState.IDLE);
		
		//NEW!
		this.job.swapJob(EnumJob.HUNTER);
		
		//Job cleanup stuff
		this.setCurrentSlot(0);
		if (this.fishEntity != null) {
			this.fishEntity.catchFish();
		}
	}
	
	protected boolean canDespawn()
    {
        return false;
    }
	
    
	
	public void setOccupationItems() {
		if (this.inventory.mainInventory[0] != null) {
			System.out.println("job items being added to populated inventory!");
		}
	}
	
	
    
	public EnumActState getCurrentAction()
	{
		return currentAction;
	}
	
	@Override
	public boolean interact(EntityPlayer entityplayer)
    {
		
		
		return false;
    }
	
	public boolean isEnemy(Entity entity1) {
		if (entity1 instanceof c_EnhAI_Old) {
			if (dipl_team != ((c_EnhAI_Old) entity1).dipl_team) {
				return true;
			} else {
				return false;
			}
		}
		/*if (entity1 == mod_EntMover.mc.thePlayer && this.dipl_hostilePlayer) {
			return true;
		}*/
		if(entity1 instanceof EntityLiving && !(entity1 instanceof EntityCreeper || entity1 instanceof EntityEnderman) && !(entity1 == this) && (entity1 instanceof EntityAnimal || entity1 instanceof EntityMob) && !(entity1 instanceof c_EnhAI_Old) ) {
			return true;
		}
		return false;
	}
	
	public void alertHunters(Entity target) {
		
		int alertRange = 128;
		int alertCount = 0;
		int alertCountMax = 5;
		
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, boundingBox.expand(alertRange, alertRange/2, alertRange));
		
        for(int j = 0; j < list.size(); j++)
        {
            Entity entity1 = (Entity)list.get(j);
            if (entity1 instanceof c_EnhAI_Old) {
            	if (((c_EnhAI_Old) entity1).job.getJob() == EnumJob.HUNTER) {
            		//System.out.println(list.size() + " size");
            		if (((c_EnhAI_Old) entity1).job.getJobState() == EnumJobState.IDLE && ((c_EnhAI_Old) entity1).currentAction == EnumActState.IDLE && ((c_EnhAI_Old) entity1).entityToAttack == null) {
            			if (((c_EnhAI_Old) entity1).job.getJobClass().sanityCheck(target)) {
	            			((c_EnhAI_Old) entity1).huntTarget(target);
	            			//System.out.println(entity1 + " alerted");
	            			alertCount++;
	            			if (alertCount > alertCountMax) return;
            			}
            		}
            	}
            }
        }
	}
	
	public void moveEntityWithHeading(float f, float f1) {
		double d = posX;
        double d1 = posY;
        double d2 = posZ;
		super.moveEntityWithHeading(f, f1);
		
		
		
		if (wasInWater) {
			if (isCollidedHorizontally && hasPath() && !inWater()) {
				//if (isCollidedHorizontally && isOffsetPositionInLiquid(motionX, ((motionY + 0.60000002384185791D) - posY) + posY, motionZ)) {
					this.motionY = 0.7;
				//}
			}
		}
		
		if (!inWater()) {
			wasInWater = false;
		} else {
			wasInWater = true;
		}
		fakePlayer.addMovementStat(posX - d, posY - d1, posZ - d2);
		
		if (onGround)
        {
            //int k = Math.round(MathHelper.sqrt_double(d * d + d2 * d2) * 100F);
            //if (k > 0)
			//System.out.println(Math.abs(this.motionX) * Math.abs(this.motionZ));
			if (Math.abs(this.motionX) * Math.abs(this.motionZ) > 0.0002)
            {
            	//System.out.println(0.01F * (float)k * 0.01F);
            	fakePlayer.addExhaustion(0.0022F);
            }
        }
	}
	
	public boolean inWater()
    {
        return worldObj.isMaterialInBB(boundingBox.expand(-0.10000000149011612D, -0.40000000596046448D, -0.10000000149011612D), Material.water);
    }
	
	public void fixEnemyTargetting() {
		int range = 3;
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(range, range, range));
        for(int j = 0; j < list.size(); j++)
        {
            Entity entity1 = (Entity)list.get(j);
            if(entity1 instanceof EntityCreature)
            {
            	Entity targ = ((EntityCreature)entity1).getEntityToAttack();
            	if (targ instanceof EntityPlayer && ((EntityPlayer)targ).username == "fakePlayer") {
            		//((EntityCreature)entity1).setEntityToAttack(null);
            		//System.out.println("fakeplayer de-targeted");
            	}
            }
        }
	}
	
	//returns: -1 = not checking, timeout in progress, 0 = no danger, 1 = found danger
	public int checkSurroundings() {
		
		fixEnemyTargetting();
		
		//if player or some hostile gets close, if not hunter perhaps run back to the village, find a hunter, update his job and have him get it
		if (checkAreaDelay < System.currentTimeMillis()) {
			checkAreaDelay = System.currentTimeMillis() + 1500 + rand.nextInt(1000);
			
			float closest = 9999F;
			Entity clEnt = null;
			
			List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(checkRange, checkRange/2, checkRange));
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);
	            if(isEnemy(entity1))
	            {
	            	if (((EntityLiving) entity1).canEntityBeSeen(this)) {
	            		float dist = this.getDistanceToEntity(entity1);
            			if (dist < closest) {
            				closest = dist;
            				clEnt = entity1;
            			}
	            		
	            		//break;
	            	}
	            }
	        }
	        
	        if (clEnt != null) { 
	        	alertHunters(clEnt);
	        	return 1;
	        } else {
	        	return 0;
	        }
	        
	        
		} else {
			return -1;
		}
		
		
		
		//if threat - setstate moving -> village
	}
	
	public boolean checkHealth() {
		if (health < getMaxHealth() * 0.75) {
			return true;
		}
		return false;
	}
	
	public boolean checkHunger() {
		if (/*health < getMaxHealth() / 4 * 3 && */getFoodLevel() <= 17) {
			//System.out.println("OH: " + + fakePlayer.foodStats.getFoodLevel()); 
			if (eat()) {
				//System.out.println("NH: " + fakePlayer.foodStats.getFoodLevel());
			} else {
				//fallback();
				if (job.getJob() != EnumJob.FINDFOOD) {
					swapJob(EnumJob.FINDFOOD);
					return true;
				}
			}
			//try heal
		}
		return false;
		
	}
	
	public void updateJob() {
		//setEntityDead();
		
		//NEW SYSTEM!
		//if (job.curJobs.size() > 0) {
		job.tick();
		//}
		
		if (true) return;
		
		/*if (occupation == EnumKoaOccupation.mFISHERMAN || occupation == EnumKoaOccupation.fFISHERMAN) {
			jobFisherman();
		} else if (occupation == EnumKoaOccupation.mHUNTER || occupation == EnumKoaOccupation.fHUNTER) {
			jobHunter();
		} else if (occupation == EnumKoaOccupation.FINDFOOD) {
			jobFindHealth();
		} else if (occupation == EnumKoaOccupation.TRADING) {
			jobTrading();
		}*/
	}
	
	public void updateAI() {
		//setEntityDead();
		
		//dipl_hostilePlayer = false;
		//entityToAttack = null;
		
		//temp overrides, test settings?
		//maxDistanceFromHome = 256F;
		//maxReach_Ranged = 12F;
		
		//stuff that needs adjusting depending on active job?
		if (job.getJob() == EnumJob.FISHERMAN) {
			entityCollisionReduction = 0.9F;
		} else {
			entityCollisionReduction = 0.8F;
		}
		
		//make all hungry!
		//this.fakePlayer.foodStats.setFoodLevel(3);
		//PFQueue.maxNodeIterations = 3000;
		
		
		//
		// fake player targetting fix and etc
		//if (entityToAttack instanceof EntityTropicraftPlayerProxy || entityToAttack instanceof EntityPlayer/* && entityToAttack != ModLoader.getMinecraftInstance().thePlayer*/) { entityToAttack = null; }
		
		if (this.openedChest > 0) {
			this.openedChest--;
			if (this.openedChest == 0) closeHomeChest();
		}
		
		if (groupInfoDelay > 0) groupInfoDelay--;
		 
		if (dipl_spreadInfoDelay > 0) dipl_spreadInfoDelay--;
		
		
		//facingWater = false;
		if (!isInWater()) {
			wasInWater = false;
			int yyStart = (int)posY;
	    	for (int yy = 1; yy > -20; yy--) {
	    		int id = getAimID(yy);
	    		if (isBlockWater(id)) {
	    			facingWater = true;
	    		} else if (id != 0) {
	    			//hit solid, stop scanning!
	    			break;
	    		}
	    		//if (getAimBlockID(-yy) == 9) {
	    		/*if (facingWater = getAimIsWater(yy)) {
	    			break;
	    		}*//* else if (getAimBlockID(-yy) != 0) {
	    			break;
	    		}*/
	    	}
		} else {
			wasInWater = true;
			facingWater = false;
		}
    	
    	//System.out.println("?! " + id);
		
		//super temp debug - keeps them fishermen or hunters		//hai;D sexy
		//if (occupation != EnumJob.mHUNTER && occupation != EnumJob.mFISHERMAN) occupation = EnumJob.mFISHERMAN;
		
		//moar temp - keeps them fishermen
		//if (occupation != EnumJob.mFISHERMAN) occupation = EnumJob.mFISHERMAN;
		
		/*if (occupation == EnumJob.mHUNTER && occupationState == occupationState.IDLE) {
			occupation = EnumJob.mFISHERMAN;
		} else if (occupation == EnumJob.mFISHERMAN && occupationState == occupationState.IDLE) {
			occupation = EnumJob.mHUNTER;
		}*/
		
		if (homeX == 0) getGroupInfo(EnumInfo.HOME_COORD);
		if (this.dipl_spreadInfoDelay == 0) {
			dipl_spreadInfoDelay = 20 + rand.nextInt(10);
			this.getGroupInfo(EnumInfo.DIPL_WARN);
		}
		
		//test
		//if (mod_EntMover.masterDebug && Keyboard.isKeyDown(Keyboard.KEY_HOME)/* && homeX == 0*//* (mod_EntMover.testHomeX != homeX || mod_EntMover.testHomeY != homeY || mod_EntMover.testHomeZ != homeZ)*/) {
			//System.out.println("this ent updated coords: " + this);
			//homeX = mod_EntMover.testHomeX;
			//homeY = mod_EntMover.testHomeY;
			//homeZ = mod_EntMover.testHomeZ;
		//}
		
		
		//if (Keyboard.isKeyDown(Keyboard.KEY_HOME)) {
			//homeX = (int)(ModLoader.getMinecraftInstance().thePlayer.posX + 0.5F);
			//homeY = (int)(ModLoader.getMinecraftInstance().thePlayer.posY - 2F);
			//homeZ = (int)(ModLoader.getMinecraftInstance().thePlayer.posZ + 0.5F);
			//System.out.println(worldObj.getBlockId(homeX, homeY, homeZ));
		//}
		
		//dont comment this out, nbt might break, comment out part below
		if (oldName == null) {
			oldName = name;
		}
		
		int pfNodes = 0;
		if (this.pathToEntity != null && this.pathToEntity.points != null) {
			pfNodes = this.pathToEntity.points.length;
		}
		if (debug) {
			/*name = new StringBuilder().append(oldName + ": " + currentAction + " | " + health
					+ " | " + fakePlayer.foodStats.getFoodLevel() + " | " + fakePlayer.foodStats.getFoodSaturationLevel()
					+ " | " + occupation + " -> " + occupationState + " - " + walkingTimeout + "|" + (Integer)Behaviors.getData(this, DataTypes.noMoveTicks)
					+ "|" + this.facingWater + "|" + pfNodes).toString();*/
			
			name = new StringBuilder().append(oldName + ": " + health + "|" + getFoodLevel() + " | " + job.getJob() + " -> " + job.getJobState() + "|" + currentAction + "|" + pfNodes + "|").toString();
		} else {
			name = oldName;
		}
		//oldName = getName(gender);
		//AI Timeouts
		if (pfTimeout > 0) { pfTimeout--; }
		
		//Base class free vanilla mob ai awareness increasing
		if (enhancedAIDelay-- <= 0) {
			enhancedAIDelay = 100 + rand.nextInt(50);
			//Behaviors.enhanceMonsterAI(this);
		}
		
		//Safety awareness stuff, basic job overriding depending on priority implementation
		dangerLevel = 0;
		if (checkSurroundings() == 1) dangerLevel = 1;
		if (checkHealth()) dangerLevel = 2;
		checkHunger();
		
		//Safety overrides
		
		//Safe
		if (dangerLevel == 0) {
			updateJob();
			this.moveSpeed = 0.7F;
		//Enemy detected? (by alert system?)
		} else if (dangerLevel == 1) {
			//no change for now
			updateJob();
			this.moveSpeed = 0.7F;
		//Low health, avoid death
		} else if (dangerLevel == 2) {
			
			//maybe add exempt for hunters so they can hit and run
			//if (koaName.startsWith("Kanoa")) {
				//System.out.println("sdfsdf");
			//}
			//If nothing to avoid
			if (!job.getJobClass().avoid(true)) {
				//no danger in area, try to continue job
				updateJob();
			} else {
				if (job.getJob() == EnumJob.HUNTER) {
					if (job.getJobClass().hitAndRunDelay == 0 && this.getDistanceToEntity(lastFleeEnt) > 6F) {
						job.getJobClass().hitAndRunDelay = cooldown_Ranged+1;
						entityToAttack = lastFleeEnt;
						if (entityToAttack != null) faceEntity(entityToAttack, 180F, 180F);
					} else {
						entityToAttack = null;
					}
				} else if (job.getJob() == EnumJob.FISHERMAN) {
					if (this.fishEntity != null) this.fishEntity.catchFish();
					if (rand.nextInt(5) == 0) {
						entityToAttack = null;
					} else {
						
					}
				}
				//code to look ahead 1 node to speed up the pathfollow escape
				if (this.pathToEntity != null && this.pathToEntity.points != null) {
					int pIndex = this.pathToEntity.pathIndex+1;
					if (pIndex < this.pathToEntity.points.length) {
						if (this.worldObj.rayTraceBlocks(Vec3.createVectorHelper((double)pathToEntity.points[pIndex].xCoord + 0.5D, (double)pathToEntity.points[pIndex].yCoord + 1.5D, (double)pathToEntity.points[pIndex].zCoord + 0.5D), Vec3.createVectorHelper(posX, posY + (double)getEyeHeight(), posZ)) == null) {
							this.pathToEntity.pathIndex++;
						}
					}
				}
				this.moveSpeed = 0.9F;
				//System.out.println(occupation + " - avoid!");
			}
		}
		//System.out.println(occupation + " - ! " + dangerLevel);
		if (occupationReady) {
			
		}
		
		if (currentAction == EnumActState.IDLE && job.getJobState() == EnumJobState.IDLE) {
			
			//Anti clump code
			if (this.pathToEntity != null && this.pathToEntity.points != null && this.pathToEntity.points.length > 0) {
				job.getJobClass().walkingTimeout--;
				if (job.getJobClass().walkingTimeout <= 0) {
					//PFQueue.getPath(this, (int)targX, (int)targY, (int)targZ, maxPFRange);
					//PFQueue.getPath(var0, var1, MaxPFRange);
					//setState(EnumActState.WALKING);
					//this.setPathExToEntity(null);
					//walkingTimeout = 600;
				}
			}
			
			
			//slaughter entitycreature ai update function and put idle wander invoking code here
	        if(!hasAttacked && (!hasPath() && rand.nextInt(80) == 0 || fleeingTick > 0 || rand.nextInt(80) == 0))
	        {
	        	if (this.getDistance(homeX, homeY, homeZ) < this.maxDistanceFromHome) {
	        		updateWanderPath();
	        		
	        	} else {
	        		int randsize = 8;
	        		walkTo(this, homeX+rand.nextInt(randsize) - (randsize/2), homeY+1, homeZ+rand.nextInt(randsize) - (randsize/2), this.maxPFRange, 600);
	        	}
	        } else {
	        	if (!hasPath()) {
	        		lookForItems();
	        	}
	        }
		} else if (currentAction == EnumActState.FIGHTING) {
			actFight();
		} else if (currentAction == EnumActState.WALKING) {
			actWalk();
		} else if (currentAction == EnumActState.FOLLOWING) {
			actFollow();
		}
		
	}
	
	public void setState(EnumActState eka) {
		currentAction = eka;
		if (eka == EnumActState.IDLE) {
			//occupationReady = true;
		} else {
			//occupationReady = false;
		}
		if (eka == EnumActState.FIGHTING) {
			
			//this.setCurrentItem(Item.bow.shiftedIndex);
		} else if (eka == EnumActState.IDLE) {
			//this.setCurrentItem(mod_tropicraft.fishingRodTropical.shiftedIndex);
		}
		//System.out.println("cur action: " + currentAction);
	}
	
	public void walkTo(Entity var1, int x, int y, int z, float var2, int timeout) {
		walkTo(var1, x, y, z, var2, timeout, 0);
	}
	
	public void walkTo(Entity var1, int x, int y, int z, float var2, int timeout, int priority) {
		PFQueue.getPath(this, x, y, z, maxPFRange, priority);
		setState(EnumActState.WALKING);
		job.getJobClass().walkingTimeout = timeout;
	}
	
	public int groupInfoDelay = 0;
	public void getGroupInfo(EnumInfo eki) {
		if (groupInfoDelay > 0) return;
		groupInfoDelay = 10;
		if (eki == EnumInfo.HOME_COORD) {
			c_EnhAI_Old koa = (c_EnhAI_Old)getEnt(false, true); 
			if (koa != null) {
				homeX = koa.homeX;
				homeY = koa.homeY;
				homeZ = koa.homeZ;
			}
		} else if (eki == EnumInfo.DIPL_WARN) {
			int range = 64;
			List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(range, range/2, range));
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);
	            if (entity1 instanceof c_EnhAI_Old) {
	            	if (((c_EnhAI_Old) entity1).dipl_team == this.dipl_team) {
	            		((c_EnhAI_Old) entity1).dipl_hostilePlayer = dipl_hostilePlayer;
	            	}
	            }
	        }
		}
	}
	
	public Entity getEnt(boolean ally, boolean koa) {
		int range = 64;
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(range, range/2, range));
        for(int j = 0; j < list.size(); j++)
        {
            Entity entity1 = (Entity)list.get(j);
            if (koa) {
            	if (entity1 instanceof c_EnhAI_Old) {
            		return entity1;
            	}
            } else if((!ally && isEnemy(entity1)) || (ally && !isEnemy(entity1))) {
            	return entity1;
            }
        }
        return null;
	}
	
	// // JOB CODE - FISHERMAN \\
	
	
	
	// \\ JOB CODE - FISHERMAN //
	
	// // JOB CODE - HUNTER \\
	
	
	
	// \\ JOB CODE - HUNTER //
	
	// // JOB CODE - FIND HEALTH \\
	
	
	
	// \\ JOB CODE - FIND HEALTH //
	
	// // JOB CODE - TRADING \\
	
	
	
	// \\ JOB CODE - TRADING //
	
	public boolean isIdle() {
		if (this.job.getJobState() == EnumJobState.IDLE && currentAction == EnumActState.IDLE) return true;
		return false;
	}
	
	public boolean isIdleWalking() {
		if (this.job.getJobState() == EnumJobState.IDLE && (currentAction == EnumActState.IDLE || currentAction == EnumActState.WALKING)) return true;
		return false;
	}
	
	public boolean canTrade() {
		if (dipl_hostilePlayer) return false;
		if (this.job.getJobState() == EnumJobState.IDLE && (currentAction == EnumActState.IDLE || currentAction == EnumActState.WALKING)) return true;
		
		if ((job.getJob() == EnumJob.FISHERMAN) && this.job.getJobState() == EnumJobState.W2) return true;
		
		return false;
	}
	
	public int getItemCount(int id) {
		int count = 0;
		for(int j = 0; j < inventory.mainInventory.length; j++)
        {
            if(inventory.mainInventory[j] != null && inventory.mainInventory[j].itemID == id)
            {
            	count += inventory.mainInventory[j].stackSize;
            }
        }
		
		return count;
	}
	
	//transferCount: -1 for all, foodOverride: makes id not used, scans for ItemFood
	public void transferItems(IInventory invFrom, IInventory invTo, int id, int transferCount, boolean foodOverride) {

		int count = 0;
		for(int j = 0; j < invFrom.getSizeInventory(); j++)
        {
			//
			ItemStack ourStack = invFrom.getStackInSlot(j);
            if(ourStack != null && (ourStack.itemID == id || ourStack.getItem() instanceof ItemFood))
            {
            	for (int k = 0; k < invTo.getSizeInventory(); k++) {
            		ItemStack theirStack = invTo.getStackInSlot(k);
            		
            		
            		
            		if(theirStack == null) {
            			//no problem
            			/*theirStack = ourStack.copy();
            			invTo.setInventorySlotContents(k, theirStack);
            			invFrom.setInventorySlotContents(j, null);*/
            			
            			int space = 64;
            			
            			int addCount = ourStack.stackSize;
            			
            			//if (space < ourStack.stackSize) addCount = space;
            			if (transferCount < addCount && transferCount != -1) addCount = transferCount;
            			
            			//transfer! the sexyness! lol haha i typ so gut ikr
            			ourStack.stackSize -= addCount;
            			//theirStack.stackSize += addCount;
            			invTo.setInventorySlotContents(k, new ItemStack(ourStack.itemID, addCount, ourStack.getItemDamage()));
            			transferCount -= addCount;
            			
            			if (ourStack.stackSize == 0) {
            				invFrom.setInventorySlotContents(j, null);
	            			break;
            			}
            			
            			if (transferCount <= 0) {
            				//System.out.println("final transferCount: " + transferCount);
            				return;
            			}
            			
            			//break;
            		} else if (ourStack.itemID == theirStack.itemID && theirStack.stackSize < theirStack.getMaxStackSize()) {
            			int space = theirStack.getMaxStackSize() - theirStack.stackSize;
            			
            			int addCount = ourStack.stackSize;
            			
            			if (space < ourStack.stackSize) addCount = space;
            			if (transferCount < addCount && transferCount != -1) addCount = transferCount;
            			
            			//transfer! the sexyness! lol haha i typ so gut ikr
            			ourStack.stackSize -= addCount;
            			theirStack.stackSize += addCount;
            			transferCount -= addCount;
            			
            			if (ourStack.stackSize == 0) {
            				invFrom.setInventorySlotContents(j, null);
	            			break;
            			}
            			
            			if (transferCount <= 0) {
            				//System.out.println("final transferCount: " + transferCount);
            				return;
            			}
            		}
            	}
            }
        }
	}
	
	public void openHomeChest() {
		if (isChest(this.worldObj.getBlockId(homeX, homeY, homeZ))) {
			TileEntityChest chest = (TileEntityChest)worldObj.getBlockTileEntity(homeX, homeY, homeZ);
			if (chest != null) {
				openedChest = 10;
				chest.openChest();
			}
		}
	}
	
	public void closeHomeChest() {
		if (isChest(this.worldObj.getBlockId(homeX, homeY, homeZ))) {
			TileEntityChest chest = (TileEntityChest)worldObj.getBlockTileEntity(homeX, homeY, homeZ);
			if (chest != null) {
				openedChest = 0;
				chest.closeChest();
			}
		}
		
	}
	
	public boolean isChest(int id) {
		if (id == Block.chest.blockID) {
			return true;
		}
		return false;
	}
	
	public void takeItems(int x, int y, int z, int id, boolean food) {
		if (isChest(this.worldObj.getBlockId(x, y-1, z))) {
			y--;
		}
		boolean transferred = false;
		if (isChest(this.worldObj.getBlockId(x, y, z))) {
			TileEntityChest chest = (TileEntityChest)worldObj.getBlockTileEntity(x, y, z);
			if (chest != null) {
				
				openHomeChest();
				
				transferItems(chest, inventory, id, 6, food);
			}
		}
	}
	
	public void setInventorySlotContents(TileEntityChest chest, int i, ItemStack itemstack)
    {
		ItemStack is = chest.getStackInSlot(i);
		//chest.chestContents[i] = itemstack;
        /*if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
        {
            itemstack.stackSize = getInventoryStackLimit();
        }
        onInventoryChanged();*/
    }
	
	
	
	public void huntTarget(Entity ent) {
		PFQueue.getPath(this, ent, maxPFRange);
		this.entityToAttack = ent;
		setState(EnumActState.FIGHTING);
	}
	
	public void actFight() {
		//a range check maybe, but why, strafing/dodging techniques or something, lunging forward while using dagger etc...
		this.name = this.name;
		this.pathToEntity = this.pathToEntity;
		if (entityToAttack == null || entityToAttack.isDead) {
			entityToAttack = null;
			setState(EnumActState.IDLE);
		}
	}
	
	public void actWalk() {
		job.getJobClass().walkingTimeout--;
		//System.out.println(this.getDistance(targX, targY, targZ));
		if (this.getDistance(targX, targY, targZ) < 2F || !hasPath()) {
			this.setPathToEntity((PathEntityEx)null);
			
			setState(EnumActState.IDLE);
			
			//this.moveSpeed = 0.0F;
	        
		} else if (job.getJobClass().walkingTimeout <= 0) {
			setState(EnumActState.IDLE);
			//this.moveSpeed = 0.7F;
		}
	}
	
	//old code, remake entirely
	public void actFollow() {
		//temp
		EntityLiving entityplayer = null;
		followTarget = entityplayer;
		
		if (followTarget != null) {			
			//Player follow logic
	        if(entityplayer != null && rand.nextInt(30) < 3)
	        {
	            float f = entityplayer.getDistanceToEntity(this);
	            if(f > 10F)
	            {
	                getPathOrWalkableBlock(entityplayer, f);
	            }
	        }
		}
	}
	
	
	
	public long itemLookDelay;
    public void lookForItems() {
    	itemSearchRange = 10;
    	if (itemLookDelay < System.currentTimeMillis()) {
    		itemLookDelay = System.currentTimeMillis() + 500;
    	
	    	List var3 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(itemSearchRange*1.0D, itemSearchRange*1.0D, itemSearchRange*1.0D));
	
	        if(var3 != null) {
	            for(int var4 = 0; var4 < var3.size(); ++var4) {
	                Entity var5 = (Entity)var3.get(var4);
	
	                if(!var5.isDead && var5 instanceof EntityItem) {
	                	EntityItem ent = (EntityItem)var5;
	                	if (wantedItems.contains(ent.item.getItem().shiftedIndex)) {
		                	if (this.canEntityBeSeen(var5)) {
		                		//if (this.team == 1) {
		                		if (!var5.isInsideOfMaterial(Material.water)) {
		                			targetItem(var5);
		                		}
		                	}
	                	}
	                    //var5.onCollideWithPlayer(fakePlayer);
	                } else if (var5 instanceof EntityXPOrb) {
	                	if (this.canEntityBeSeen(var5)) {
	                		//if (this.team == 1) {
	                		if (!var5.isInsideOfMaterial(Material.water)) {
	                			targetItem(var5);
	                		}
	                	}
	                }
	            }
	        }
    	}
    }
    
    public void targetItem(Entity item) {
    	//this.setPathToEntity(worldObj.getPathToEntity(this, item, itemSearchRange+2F));
    	PFQueue.getPath(this, item, itemSearchRange+2F);
    	if (this.hasPath()) {
    		setState(EnumActState.WALKING);
    	}
    }
	
	public void readEntityFromNBT(NBTTagCompound var1) {
        super.readEntityFromNBT(var1);
        
        try {
	        targX = var1.getInteger("targX");
	        targY = var1.getInteger("targY");
	        targZ = var1.getInteger("targZ");
	        
	        homeX = var1.getInteger("homeX");
	        homeY = var1.getInteger("homeY");
	        homeZ = var1.getInteger("homeZ");
	        
	        initJobAndStates(EnumJob.get(var1.getInteger("primaryOccupation")), false);
	        currentAction = EnumActState.get(var1.getInteger("currentAction"));
	        
	        job.getJobClass().tradeTimeout = var1.getInteger("tradeTimeout");
	        
	        name = var1.getString("name");
	        dipl_hostilePlayer = var1.getBoolean("dipl_hostilePlayer");
	        homeZ = var1.getInteger("homeZ");
	        //oldName = var1.getString("oldName");
		} catch (Exception ex) { System.out.println(ex); }
	}
	
	public void writeEntityToNBT(NBTTagCompound var1) {
        super.writeEntityToNBT(var1);
        
        try {
	        var1.setInteger("targX", targX);
	        var1.setInteger("targY", targY);
	        var1.setInteger("targZ", targZ);
	        
	        var1.setInteger("homeX", homeX);
	        var1.setInteger("homeY", homeY);
	        var1.setInteger("homeZ", homeZ);
	        
	        var1.setInteger("currentAction", currentAction.ordinal());
	        var1.setInteger("occupation", job.getJob().ordinal());
	        var1.setInteger("primaryOccupation", job.priJob.ordinal());
	        
	        var1.setInteger("tradeTimeout", job.getJobClass().tradeTimeout);
	        
	        if (oldName != null && oldName != "") name = oldName;
	        var1.setString("name", name);
	        
	        var1.setBoolean("dipl_hostilePlayer", dipl_hostilePlayer);
	        var1.setInteger("dipl_team", this.dipl_team.ordinal());
	        
	        
	        /*if (oldName != null && oldName != "") {
	        	var1.setString("oldName", oldName);
	        }*/
        } catch (Exception ex) { System.out.println(ex); }
	}
	
	public void noMoveTriggerCallback() {
		if (currentAction == EnumActState.IDLE && job.getJobState() == EnumJobState.IDLE) {
	    	setEntityToAttack(null);
		}
		setPathExToEntity(null);
    }
	
	@Override
	public boolean attackEntityFrom(DamageSource damagesource, int i) {
		//Diplo update
		/*if (damagesource.getEntity() == mod_EntMover.mc.thePlayer) {
			this.dipl_hostilePlayer = true;
			this.getGroupInfo(EnumKoaInfo.DIPL_WARN);
		}*/
		
		if (job.getJob() == EnumJob.HUNTER) {
			job.getJobClass().hitHook(damagesource, i);
		}
		return super.attackEntityFrom(damagesource, i);
	}
	
	
	
	
	
	
	public boolean getCanSpawnHere()
    {
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(boundingBox.minY);
        int k = MathHelper.floor_double(posZ);
        int level = worldObj.getBlockLightValue(i, j, k);
        return worldObj.getBlockLightValue(i, j, k) <= 13 && worldObj.checkIfAABBIsClear(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).size() == 0 && !worldObj.isAnyLiquid(boundingBox);
        //return worldObj.getBlockId(i, j - 1, k) == Block.grass.blockID/* && worldObj.getFullBlockLightValue(i, j, k) > 8*/;
    }
	
	protected String getLivingSound()
    {
        return null;
    }
	
	
	protected static final ItemStack defaultHeldItem;
    
    
    
    static 
    {
        defaultHeldItem = new ItemStack(Item.fishingRod, 1);
    }



	@Override
	public boolean canUseLadders() {
		// TODO Auto-generated method stub
		return false;
	}
}
