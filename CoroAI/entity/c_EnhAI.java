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

public class c_EnhAI extends c_PlayerProxy implements c_IEnhAI
{

	public int entID = -1;
	
	public boolean debug = false;
	
	public EnumActState currentAction;
	public String name;
    
    public Random rand;
	public int jobTimeout;
	
	//Diplomatic fields
	public boolean dipl_hostilePlayer = false;
	public EnumTeam dipl_team = EnumTeam.HOSTILES;
	public int dipl_spreadInfoDelay = 0;
	
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
	public int openedChest = 0;
    
    public JobManager job;
    
    //priority stuff?
    public boolean occupationReady = true;
    public int dangerLevel = 0;
	
	//for debugging
    public String oldName;
	
	public boolean facingWater = false;
	public boolean wasInWater = false;
	public float oldMoveSpeed;
	public float fleeSpeed;
	public boolean fleeing = false;
	
	public boolean enhanceAIEnemies = false;
	public int enhancedAIDelay = 100;
	
	//flee based stuff
	public int prevHealth;
	public Entity lastFleeEnt;
	public boolean tryingToFlee;
	
	public float lungeFactor = 1.0F;

	private int jumpDelay;
	
	public c_EnhAI(World world) 
	{
		super(world);		
		
		//new
		this.job = new JobManager(this);
		
		this.rand = new Random();
		
		initJobAndStates(EnumJob.FISHERMAN, false);
		
		homeX = -114;
		homeY = 64;
		homeZ = 908;
		
		homeX = 51;
		homeY = 63;
		homeZ = 311;
		//name = ".";
		
		moveSpeed = 0.8F;
		
		fleeSpeed = 1.1F;
		oldMoveSpeed = moveSpeed;
	}
	
	public void initJobAndStates(EnumJob job) {
		initJobAndStates(job, true);
	}
	
	public void initJobAndStates(EnumJob job, boolean initItems) {
		
		/*if (this.rand.nextInt(2) == 0) {
			job = EnumJob.HUNTER;
		} else {
			job = EnumJob.FISHERMAN;
		}*/
		//System.out.println("init job " + job);
		
		this.job.setPrimaryJob(job);
		
		
		
		//this.job.swapJob(job);
		this.job.clearJobs();
		if (job == EnumJob.HUNTER) {
			addJob(EnumJob.FINDFOOD);
	        addJob(EnumJob.HUNTER);
		} else if (job == EnumJob.FISHERMAN) {
			addJob(EnumJob.FINDFOOD);
	        addJob(EnumJob.FISHERMAN);
		} else if (job == EnumJob.GATHERER) {
			addJob(EnumJob.GATHERER);
		} else if (job == EnumJob.INVADER) {
			addJob(EnumJob.INVADER);
		} else {
			addJob(EnumJob.UNEMPLOYED);
		}
		
		if (initItems) {
			
			setOccupationItems();
			if (entID == -1) entID = rand.nextInt(999999999);
		}
	}
	
	public void swapJob(EnumJob job) {
		setState(EnumActState.IDLE);
		this.job.swapJob(job);
	}
	
	public void addJob(EnumJob job) {
		//setState(EnumActState.IDLE);
		this.job.addJob(job);
	}
	
	public void setOccupationItems() {
		if (this.inventory.mainInventory[0] != null) {
			System.out.println("Possible error - job items being added to populated inventory!");
		}
		job.enumToJob(job.priJob).setJobItems();
	}
	
	public EnumActState getCurrentAction()
	{
		return currentAction;
	}
	
	public boolean isEnemy(Entity entity1) {
		if (entity1 instanceof c_EnhAI) {
			if (dipl_team != ((c_EnhAI) entity1).dipl_team) {
				return true;
			} else {
				return false;
			}
		} else {
			return c_CoroAIUtil.isEnemy(this, entity1);
			
		}
	}
	
	public void alertHunters(Entity target) {
		
		int alertRange = 128;
		int alertCount = 0;
		int alertCountMax = 5;
		
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, boundingBox.expand(alertRange, alertRange/2, alertRange));
		
        for(int j = 0; j < list.size(); j++)
        {
            Entity entity1 = (Entity)list.get(j);
            if (entity1 instanceof c_EnhAI) {
            	if (((c_EnhAI) entity1).job.priJob == EnumJob.HUNTER) {
            		//System.out.println(list.size() + " size");
            		if (((c_EnhAI) entity1).job.getJobState() == EnumJobState.IDLE && ((c_EnhAI) entity1).currentAction == EnumActState.IDLE && ((c_EnhAI) entity1).entityToAttack == null) {
            			if (((c_EnhAI) entity1).job.getJobClass().sanityCheckHelp(this, target)) {
	            			((c_EnhAI) entity1).huntTarget(target);
	            			System.out.println(((c_EnhAI) entity1).name + " alerted");
	            			alertCount++;
	            			if (alertCount > alertCountMax) return;
            			}
            		}
            	}
            }
        }
	}
	
	@Override
	public void setDead() {
		//if (!this.worldObj.isRemote) System.out.println(name + ", A KOA HAS DIED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		super.setDead();
	}
	
	public double getDistanceXZ(double par1, double par5)
    {
        double var7 = this.posX - par1;
        //double var9 = this.posY - par3;
        double var11 = this.posZ - par5;
        return (double)MathHelper.sqrt_double(var7 * var7 + var11 * var11);
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
		if (fakePlayer != null) {
			try { fakePlayer.addMovementStat(posX - d, posY - d1, posZ - d2); } catch (NoSuchFieldError ex) { /*ex.printStackTrace();*/ }
			
			if (onGround)
	        {
	            //int k = Math.round(MathHelper.sqrt_double(d * d + d2 * d2) * 100F);
	            //if (k > 0)
				//System.out.println(Math.abs(this.motionX) * Math.abs(this.motionZ));
				if (Math.abs(this.motionX) * Math.abs(this.motionZ) > 0.0002)
	            {
	            	//System.out.println(0.01F * (float)k * 0.01F);
	            	try { fakePlayer.addExhaustion(0.0022F); } catch (NoSuchFieldError ex) { /*ex.printStackTrace();*/ }
	            }
	        }
		}
	}
	
	public boolean inWater()
    {
        return worldObj.isMaterialInBB(boundingBox.expand(-0.10000000149011612D, -0.40000000596046448D, -0.10000000149011612D), Material.water);
    }
	
	/*public boolean isSolidPath(Entity var1) {
        
		//System.out.println(Math.abs(this.posY - (double)this.yOffset - (var1.posY - (double)var1.yOffset)));
        return this.canEntityBeSeen(var1) && (this.getDistanceToEntity(var1) < 5.0F) && Math.abs(this.posY - (double)this.yOffset - (var1.posY - (double)var1.yOffset)) <= 3.5D;
    }*/
	
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
		if (checkAreaDelay < System.currentTimeMillis() && checkHealth()) {
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
	
	public float getMoveSpeed() {
		return moveSpeed;
	}
	
	public void pathNav() {
		if (pathToEntity != null) {
			//System.out.println("haspath");
			Vec3 curPos = this.pathToEntity.getPosition(this);
	
	        double var6 = (double)(this.width * this.width);
	
	        while (curPos != null && curPos.squareDistanceTo(this.posX, curPos.yCoord, this.posZ) < var6 * var6)
	        {
	            this.pathToEntity.incrementPathIndex();
	
	            if (this.pathToEntity.isFinished())
	            {
	            	curPos = null;
	                this.pathToEntity = null;
	            }
	            else
	            {
	            	curPos = this.pathToEntity.getPosition(this);
	            }
	        }
	        
	        
	        if (curPos != null) {
	        	int var21 = MathHelper.floor_double(this.boundingBox.minY + 0.5D);
	        	
		        double var8 = curPos.xCoord - this.posX;
	            double var10 = curPos.zCoord - this.posZ;
	            double var12 = curPos.yCoord - (double)var21;
	            
	            var8 = this.posX - curPos.xCoord;
	            var10 = this.posZ - curPos.zCoord;
	            var12 = curPos.yCoord - (double)var21;
	            
	            float var14 = (float)(Math.atan2(var10, var8) * 180.0D / Math.PI) - 90.0F;
	            float var15 = var14 - (float)this.newRotationYaw;
	
	            for (this.moveForward = this.moveSpeed; var15 < -180.0F; var15 += 360.0F)
	            {
	                ;
	            }
	
	            while (var15 >= 180.0F)
	            {
	                var15 -= 360.0F;
	            }
	
	            if (var15 > 30.0F)
	            {
	                var15 = 30.0F;
	            }
	
	            if (var15 < -30.0F)
	            {
	                var15 = -30.0F;
	            }
	
	            //this.rotationYaw += var15;//this.updateRotation2(var15, 90F, 90F);
	            //this.newRotationYaw += var15;
	            if (entityToAttack != null) {
	            	
	            	//this.faceEntity(entityToAttack, 90F, 90F);
	            	//this.faceCoord((int)curPos.xCoord, (int)curPos.yCoord, (int)curPos.zCoord, 90F, 90F);
	            }
	            
	            //this.moveEntityWithHeading(this.moveStrafing, this.moveForward);
	        }
	        
	        //finally, update with movehelper
	        if (curPos != null)
	        {
	            /*if (rand.nextInt(10) == 0) *///this.getMoveHelper().func_48187_a(curPos.xCoord, curPos.yCoord, curPos.zCoord, 0.23F);
	        }
		}
	}
	
	public void setVelocity(double par1, double par3, double par5)
    {
        this.motionX = par1;
        this.motionY = par3;
        this.motionZ = par5;
        mX = par1;
        mY = par3;
        mZ = par5;
    }
	
	public double mX;
	public double mY;
	public double mZ;
	
	public void onLivingUpdate2() {
		//setDead();
		
		
	}
	
	//@Override
	public void onLivingUpdate() {
		
		//setDead();
		//super.onLivingUpdate();
		if (worldObj.isRemote) {
			//System.out.println(mX + " - " + mZ);
			//motionX = mX;
			//motionY = mY;
			//motionZ = mZ;
			
			//this.setCurrentSlot(this.dataWatcher.getWatchableObjectInt(22));
		}
		
		if (c_CoroAIUtil.isServer()) {
			//System.out.println("Server lunge: " + lungeFactor);
		} else {
			//System.out.println("Client lunge: " + lungeFactor);
		}
		
		float factor = 0.35F * lungeFactor;
		factor = 0.35F * lungeFactor;
		if (this.dataWatcher.getWatchableObjectInt(20) == 1) {
			//this.getMoveHelper().setMoveTo(entityToAttack.posX, entityToAttack.posY, entityToAttack.posZ, this.moveSpeed*factor);
			//this.moveSpeed = this.moveForward = 0.7F * lungeFactor;
			float smpFactor = 1.0F;
			
			if (c_CoroAIUtil.isServer()) {
				smpFactor = 0.6F;
			}
			if (worldObj.isRemote) {
				//this.moveSpeed = this.moveForward = this.oldMoveSpeed;
				//this.moveFlying(0F, 0.7F, 0.1F);
				//System.out.println("moving");
				//setAIMoveSpeed(moveSpeed * factor);
			}
			//this makes it move faster, but is glitchy client side now...
			///this.moveFlying(0F, factor * smpFactor, 0.1F);
		} else {
			//this.getMoveHelper().setMoveTo(entityToAttack.posX, entityToAttack.posY, entityToAttack.posZ, this.moveSpeed*0.35F);
			
			if (worldObj.isRemote) {
				//this.moveSpeed = this.moveForward = this.oldMoveSpeed;
				//this.moveFlying(0F, 0.7F, 0.1F);
				//setAIMoveSpeed(0);
			}
			
		}
		
		if (this.dataWatcher.getWatchableObjectInt(21) == 1) {
			if (c_CoroAIUtil.isServer()) {
				//System.out.println("Server swingArm: " + swingArm);
			} else {
				//System.out.println("Client swingArm: " + swingArm);
			}
			swingArm = true;
			this.dataWatcher.updateObject(21, 0);
		}
		
		//setAIMoveSpeed(0.24F);
		
		
		
		super.onLivingUpdate();
	}
	
	public void doMovement() {
		
		//setDead();
		
		if (this.deathTime > 0) return;
		
		float factor = 0.35F * lungeFactor;
		
		//Lunge speed!
		if (fleeing) {
			//this.func_48098_g(moveSpeed);
			this.dataWatcher.updateObject(20, 1);
		
		} else if (entityToAttack != null && this.isSolidPath(entityToAttack) && this.onGround)	{
			//if (moveForward == 0F) {
			//if (getNavigator().func_48670_c() == null) {
				//getNavigator().func_48670_c().func_48644_d();
				//getNavigator().clearPathEntity();
				//System.out.println("what?!");
				this.getMoveHelper().setMoveTo(entityToAttack.posX, entityToAttack.posY, entityToAttack.posZ, this.getMoveHelper().getSpeed());
				this.dataWatcher.updateObject(20, 1);
				
				//jump over drops
				
				MovingObjectPosition aim = getAimBlock(-2, true);
		    	if (aim != null) {
		    		if (aim.typeOfHit == EnumMovingObjectType.TILE) {
		    			
		    		}
		    	} else {
		    		if (this.onGround) {
		    			jump();
		    		}
		    	}
				
		    	
		    	
				//this.moveSpeed = this.moveForward = 0.7F * lungeFactor;
				//System.out.println(this.moveSpeed);
				//this.getMoveHelper().func_48186_a()
			//}
			
		//}
		/*this.moveForward = this.moveSpeed;
		this.faceEntity(entityToAttack, 30, 30);
		this.moveEntityWithHeading(this.moveStrafing, this.moveForward * 0.1F);*/
		} else if (this.getNavigator().getPath() != null) {
			this.dataWatcher.updateObject(20, 1);
		} else {
			//this.func_48098_g(moveSpeed);
			this.dataWatcher.updateObject(20, 0);
			//this.moveSpeed = this.moveForward = this.oldMoveSpeed;
		}
		
		if (this.moveForward <= 0) {
			this.dataWatcher.updateObject(20, 0);
		}
		
		if (isInWater()) {
			if (this.jumpDelay == 0) {
				if (entityToAttack != null) {
					//faceEntity(entityToAttack,30F,30F);
					//this.getMoveHelper().setMoveTo(entityToAttack.posX, entityToAttack.posY, entityToAttack.posZ, this.getMoveHelper().getSpeed());
				}
				//jump();
				//this.jumpDelay = 30;
				this.motionY += 0.03D;
				
				//pathfollow fix
				if (true) {
					if (getNavigator().getPath() != null) {
						PathEntity pEnt = getNavigator().getPath();
						int index = pEnt.getCurrentPathIndex()+1;
						//index--;
						if (index < 0) index = 0;
						if (index > pEnt.getCurrentPathLength()) index = pEnt.getCurrentPathLength()-1;
						Vec3 var1 = null;
						try {
							//var1 = pEnt.getVectorFromIndex(this, index);
							//var1 = pEnt.getVectorFromIndex(this, pEnt.getCurrentPathLength()-1);
							//if (pEnt.getCurrentPathLength() > 2) {
								var1 = pEnt.getVectorFromIndex(this, pEnt.getCurrentPathIndex());
							//}
						} catch (Exception ex) {
							//System.out.println("c_EnhAI water pf err");
							//ex.printStackTrace();
							var1 = pEnt.getVectorFromIndex(this, pEnt.getCurrentPathLength()-1);
						}
	
		                if (var1 != null)
		                {
		                	this.getMoveHelper().setMoveTo(var1.xCoord, var1.yCoord, var1.zCoord, 0.53F);
		                	double dist = this.getDistance(var1.xCoord, var1.yCoord, var1.zCoord);
		                	if (dist < 8) {
		                		//System.out.println("dist to node: " + dist);
		                		
		                	}
		                	if (dist < 2) {
		                		getNavigator().getPath().incrementPathIndex();
		                	}
		                    //
		                }
					}
				}
				
				if (false && this.isInWater())
		        {
					//this.getNavigator().setPath(null, this.moveSpeed);
					
		            double var3 = this.posY - 0.5F;
		            //this.moveFlying(this.moveStrafing, this.moveForward, 0.04F);
		            this.motionY = 0.03D;
		            
		            //this.moveEntity(this.motionX, this.motionY, this.motionZ);
		            this.motionX *= 0.800000011920929D;
		            this.motionY *= 0.800000011920929D;
		            this.motionZ *= 0.800000011920929D;
		            this.motionY -= 0.02D;
	
		            if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + var3, this.motionZ))
		            {
		                this.motionY = 0.30000001192092896D;
		            }
		        }
			}
		}
		
		if (this.onGround && this.isCollidedHorizontally) {
			jump();
		}
		
		if (this.onGround && this.jumpDelay == 0)
        {
            //this.jump();
            //this.jumpDelay = 10;
        }
		
		//fix for render flickering when entity is a weather entity
		lastTickPosX = posX;
		lastTickPosY = posY;
		lastTickPosZ = posZ;
	}
	
	@Override
	protected void entityInit()
    {
		super.entityInit();
        this.dataWatcher.addObject(20, Integer.valueOf(0)); //Move speed state
        this.dataWatcher.addObject(21, Integer.valueOf(0)); //Swing arm state
    }
	
	@Override
	public void swingItem()
    {
    	//super.swingItem();
    	//if (serverMode) {
		//swingArm = true;
    	fakePlayer.addExhaustion(0.14F);
    	this.dataWatcher.updateObject(21, 1);
    	//}
    }
	
	public boolean canSeeBlock(int x, int y, int z) {
		MovingObjectPosition mop = this.worldObj.rayTraceBlocks(Vec3.createVectorHelper(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ), Vec3.createVectorHelper(x, y, z));
		if (mop == null) return true;
		if (mop.blockX == x && mop.blockY == y && mop.blockZ == z) return true;
		return false;
	}
	
	public void updateAI() {
		//temp overrides, test settings?
		//maxDistanceFromHome = 512F;
		//maxReach_Ranged = 12F;
		//if (true) return;
		//this.setAttackTarget(null);
		//this.setEntityToAttack(null);
		//System.out.println(name);
		//setDead();
		//STUFF MOVED FROM ENTITY CREATURE
		//pathNav();
		doMovement();
		
		//if (this.fakePlayer.getFoodStats().getFoodLevel() > 14) fakePlayer.addExhaustion(0.3F);
		
		try {
			if (entityToAttack == fakePlayer) entityToAttack = null;
		} catch (Error ex) {}
		
		//syncing with nav targetting - what?
		if (entityToAttack != null && entityToAttack.isEntityAlive()) {
			
			float var2 = this.entityToAttack.getDistanceToEntity(this);

            if (this.canEntityBeSeen(this.entityToAttack))
            {
                this.attackEntity(this.entityToAttack, var2);
            }
            else
            {
                //this.attackBlockedEntity(this.entityToAttack, var2);
            }
		}
		
		double var12 = 1D;//var5.yCoord - (double)var21;
		
		if (var12 > 0.0D)
        {
            this.isJumping = true;
        }
		
		if (this.isCollidedHorizontally && this.hasPath())
		{
		    this.isJumping = true;
		    //if (onGround) jump();
		}
		
		
		
		
		
		//stuff that needs adjusting depending on active job?
		if (job.priJob == EnumJob.FISHERMAN) {
			entityCollisionReduction = 0.9F;
		} else {
			entityCollisionReduction = 0.8F;
		}
		
		//AI Timeouts
		if (pfTimeout > 0) { pfTimeout--; }
		if (groupInfoDelay > 0) groupInfoDelay--;
		if (dipl_spreadInfoDelay > 0) dipl_spreadInfoDelay--;
		if (jumpDelay > 0) jumpDelay--;
		
		//Base class free vanilla mob ai awareness increasing
		if (enhancedAIDelay-- <= 0) {
			enhancedAIDelay = 100 + rand.nextInt(50);
			if (enhanceAIEnemies) Behaviors.enhanceMonsterAI(this); // disabled!
		}
		
		if (this.openedChest > 0) {
			this.openedChest--;
			if (this.openedChest == 0) closeHomeChest();
		}
		
		if (!isInWater()) {
			wasInWater = false;
			int yyStart = (int)posY;
	    	for (int yy = 1; yy > -20; yy--) {
	    		int id = 0;//getAimID(yy);
	    		if (isBlockWater(id)) {
	    			facingWater = true;
	    		} else if (id != 0) {
	    			//hit solid, stop scanning!
	    			break;
	    		}
	    	}
		} else {
			wasInWater = true;
			facingWater = false;
		}
		
		if (homeX == 0) getGroupInfo(EnumInfo.HOME_COORD);
		if (this.dipl_spreadInfoDelay == 0) {
			dipl_spreadInfoDelay = 20 + rand.nextInt(10);
			this.getGroupInfo(EnumInfo.DIPL_WARN);
		}
		
		//dont comment this out, nbt might break, comment out part below
		if (oldName == null) {
			oldName = name;
		}
		
		int pfNodes = 0;
		if (this.pathToEntity != null && this.pathToEntity.points != null) {
			pfNodes = this.pathToEntity.points.length;
		}
		//test
		/*if (c_CoroAIUtil.mc.objectMouseOver != null && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD1)) {
			int x = c_CoroAIUtil.mc.objectMouseOver.blockX;
			int y = c_CoroAIUtil.mc.objectMouseOver.blockY;
			int z = c_CoroAIUtil.mc.objectMouseOver.blockZ;
			if (homeX == 0) {
				System.out.println("update home coords: " + this);
				homeX = x;
				homeY = y;
				homeZ = z;
			}
		}*/
		if (debug) {
			/*name = new StringBuilder().append(oldName + ": " + currentAction + " | " + health
					+ " | " + fakePlayer.foodStats.getFoodLevel() + " | " + fakePlayer.foodStats.getFoodSaturationLevel()
					+ " | " + occupation + " -> " + occupationState + " - " + walkingTimeout + "|" + (Integer)Behaviors.getData(this, DataTypes.noMoveTicks)
					+ "|" + this.facingWater + "|" + pfNodes).toString();*/
			
			name = new StringBuilder().append(oldName + ": " + health + "|" + getFoodLevel() + " | " + job.priJob + " -> " + job.getJobState() + "|" + currentAction + "|" + pfNodes + "|").toString();
		} else {
			name = oldName;
			//name = new StringBuilder().append(oldName + ": " + health + "|" + getFoodLevel() + " | " + job.getJob() + " -> " + job.getJobState() + "|" + currentAction + "|" + pfNodes + "|").toString();
		}
		
		//name = new StringBuilder().append(oldName + ": " + health + "|" + getFoodLevel() + " | " + job.priJob + " -> " + job.getJobState() + "|" + currentAction + "|" + pfNodes + "|" + "JOBS: " + job.debug).toString();
		
		//Safety awareness stuff, basic job overriding depending on priority implementation
		dangerLevel = 0;
		if (checkSurroundings() == 1) dangerLevel = 1;
		if (checkHealth()) dangerLevel = 2;
		job.getJobClass().checkHunger();
		
		
		
		//Safety overrides
		fleeing = false;
		//Safe
		if (dangerLevel == 0) {
			updateJob();
			this.moveSpeed = oldMoveSpeed;
			
		//Enemy detected? (by alert system?)
		} else if (dangerLevel == 1) {
			//no change for now
			updateJob();
			this.moveSpeed = oldMoveSpeed;
			
		//Low health, avoid death
		} else if (dangerLevel == 2) {
			
			//If nothing to avoid
			if (!job.getJobClass().avoid(true)) {
				fleeing = false;
				//no danger in area, try to continue job
				updateJob();
				this.moveSpeed = oldMoveSpeed;
			} else {
				fleeing = true;
				job.getJobClass().onLowHealth();
				
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
				
				PathEntity pe = this.getNavigator().getPath();
				
				if (pe != null && !pe.isFinished()) {
					
					if (this.worldObj.rayTraceBlocks(pe.getPosition(this), Vec3.createVectorHelper(posX, posY + (double)getEyeHeight(), posZ)) == null) {
						pe.incrementPathIndex();
						//System.out.println("next path!");
					}
					
					/*int pIndex = pe.pathIndex+1;
					if (pIndex < this.pathToEntity.points.length) {
						if (this.worldObj.rayTraceBlocks(Vec3.createVectorHelper((double)pathToEntity.points[pIndex].xCoord + 0.5D, (double)pathToEntity.points[pIndex].yCoord + 1.5D, (double)pathToEntity.points[pIndex].zCoord + 0.5D), Vec3.createVectorHelper(posX, posY + (double)getEyeHeight(), posZ)) == null) {
							this.pathToEntity.pathIndex++;
						}
					}*/
				}
				
				this.moveSpeed = fleeSpeed;
			}
		}
		
		if (currentAction == EnumActState.IDLE && job.getJobState() == EnumJobState.IDLE) {
			
			job.getJobClass().onIdleTick();
			
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
	}
	
	public void walkTo(Entity var1, int x, int y, int z, float var2, int timeout) {
		walkTo(var1, x, y, z, var2, timeout, 0);
	}
	
	public void walkTo(Entity var1, int x, int y, int z, float var2, int timeout, int priority) {
		PFQueue.getPath(this, x, y, z, maxPFRange, priority);
		setState(EnumActState.WALKING);
		job.getJobClass().walkingTimeout = timeout;
		targX = x;
		targY = y;
		targZ = z;
	}
	
	public int groupInfoDelay = 0;
	public void getGroupInfo(EnumInfo eki) {
		if (groupInfoDelay > 0) return;
		groupInfoDelay = 10;
		if (eki == EnumInfo.HOME_COORD) {
			c_EnhAI koa = (c_EnhAI)getEnt(false, true); 
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
	            if (entity1 instanceof c_EnhAI && this.canEntityBeSeen(entity1)) {
	            	if (((c_EnhAI) entity1).dipl_team == this.dipl_team) {
	            		((c_EnhAI) entity1).dipl_hostilePlayer = dipl_hostilePlayer;
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
            	if (entity1 instanceof c_EnhAI) {
            		return entity1;
            	}
            } else if((!ally && isEnemy(entity1)) || (ally && !isEnemy(entity1))) {
            	return entity1;
            }
        }
        return null;
	}
	
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
		
		if ((job.priJob == EnumJob.FISHERMAN) && this.job.getJobState() == EnumJobState.W2) return true;
		
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
			if (ourStack != null && ((id == -1 && !foodOverride) || ourStack.itemID == id || (ourStack.getItem() instanceof ItemFood && foodOverride)))
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
            			
            			if (ourStack.stackSize < 0) {
            				System.out.println("!! ourStack.stackSize < 0");
            			}
            			
            			//if (space < ourStack.stackSize) addCount = space;
            			if (transferCount < addCount && transferCount != -1) addCount = transferCount;
            			
            			//transfer! the sexyness! lol haha i typ so gut ikr
            			ourStack.stackSize -= addCount;
            			//theirStack.stackSize += addCount;
            			invTo.setInventorySlotContents(k, new ItemStack(ourStack.itemID, addCount, ourStack.getItemDamage()));
            			if (transferCount != -1) transferCount -= addCount;
            			
            			if (ourStack.stackSize == 0) {
            				invFrom.setInventorySlotContents(j, null);
	            			break;
            			} else if (ourStack.stackSize < 0) {
            				System.out.println("ourStack.stackSize < 0");
            			}
            			
            			if (transferCount == 0) {
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
            			if (transferCount != -1) transferCount -= addCount;
            			
            			if (ourStack.stackSize == 0) {
            				invFrom.setInventorySlotContents(j, null);
	            			break;
            			}
            			
            			if (transferCount == 0) {
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
		return c_CoroAIUtil.isChest(id);
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
				
				transferItems(chest, inventory, id, 1, food);
			}
		}
	}
	
	public void setInventorySlotContents(TileEntityChest chest, int i, ItemStack itemstack)
    {
		ItemStack is = chest.getStackInSlot(i);
    }
	
	public void huntTarget(Entity ent, int pri) {
		PFQueue.getPath(this, ent, maxPFRange, pri);
		//System.out.println("huntTarget call: " + ent);
		this.entityToAttack = ent;
		setState(EnumActState.FIGHTING);
	}
	
	public void huntTarget(Entity ent) {
		huntTarget(ent, 0);
	}
	
	public void actFight() {
		//a range check maybe, but why, strafing/dodging techniques or something, lunging forward while using dagger etc...
		if (entityToAttack == null || entityToAttack.isDead) {
			entityToAttack = null;
			setState(EnumActState.IDLE);
		}
	}
	
	public void actWalk() {
		job.getJobClass().walkingTimeout--;
		//System.out.println(this.getDistance(targX, targY, targZ));
		if (this.getDistance(targX, targY, targZ) < 2F || getNavigator().getPath() == null) {
			this.setPathToEntity((PathEntityEx)null);
			getNavigator().clearPathEntity();
			setState(EnumActState.IDLE);
		} else if (job.getJobClass().walkingTimeout <= 0) {
			setState(EnumActState.IDLE);
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
	                } else if (var5 instanceof EntityXPOrb) {
	                	if (this.canEntityBeSeen(var5)) {
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
    	PFQueue.getPath(this, item, itemSearchRange+2F);
    	if (this.hasPath()) {
    		setState(EnumActState.WALKING);
    	}
    }
	
	public void readEntityFromNBT(NBTTagCompound var1) {
        super.readEntityFromNBT(var1);
        
        
        try {
        	
        	entID = var1.getInteger("entID");
        	
        	System.out.println("nbt loaded koa id: " + entID);
        	
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
	        
	        
		} catch (Exception ex) { 
			ex.printStackTrace();
		}
	}
	
	public void writeEntityToNBT(NBTTagCompound var1) {
        super.writeEntityToNBT(var1);
        
        try {
        	
        	var1.setInteger("entID", this.entID);
        	
	        var1.setInteger("targX", targX);
	        var1.setInteger("targY", targY);
	        var1.setInteger("targZ", targZ);
	        
	        var1.setInteger("homeX", homeX);
	        var1.setInteger("homeY", homeY);
	        var1.setInteger("homeZ", homeZ);
	        
	        
	        //var1.setInteger("occupation", job.getJob().ordinal());
	        var1.setInteger("primaryOccupation", job.priJob.ordinal());
	        
	        var1.setInteger("tradeTimeout", job.getJobClass().tradeTimeout);
	        
	        if (oldName != null && oldName != "") name = oldName;
	        if (name != null) var1.setString("name", name);
	        
	        var1.setBoolean("dipl_hostilePlayer", dipl_hostilePlayer);
	        var1.setInteger("dipl_team", this.dipl_team.ordinal());
	        
	        if (currentAction != null) {
	        	var1.setInteger("currentAction", currentAction.ordinal());
	        }
	        
        } catch (Exception ex) { ex.printStackTrace(); }
	}
	
	public void noMoveTriggerCallback() {
		if (currentAction == EnumActState.IDLE && job.getJobState() == EnumJobState.IDLE) {
	    	setEntityToAttack(null);
		}
		this.getNavigator().setPath(null, 0F);
		setPathExToEntity(null);
    }
	
	@Override
	public boolean attackEntityFrom(DamageSource damagesource, int i) {
		//Diplo update
		/*if (damagesource.getEntity() == mod_EntMover.mc.thePlayer) {
			this.dipl_hostilePlayer = true;
			this.getGroupInfo(EnumKoaInfo.DIPL_WARN);
		}*/
		
		job.getJobClass().hitHook(damagesource, i);
		
		return super.attackEntityFrom(damagesource, i);
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
	
	public void setPathExToEntity(PathEntityEx pathentity)
    {
		getNavigator().setPath(PFQueue.convertToPathEntity(pathentity), getMoveSpeed() * 0.35F);
        pathToEntity = pathentity;
    }
}
