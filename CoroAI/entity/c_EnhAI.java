package CoroAI.entity;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import CoroAI.Behaviors;
import CoroAI.PFQueue;
import CoroAI.PathEntityEx;
import CoroAI.c_CoroAIUtil;
import CoroAI.c_IEnhAI;

public class c_EnhAI extends c_PlayerProxy implements c_IEnhAI
{

	public boolean debug = false;
	
	public EnumActState currentAction;
	public String name;
	public String debugInfo;
    
    public Random rand;
	public int jobTimeout;
	
	//Diplomatic fields
	public boolean dipl_hostilePlayer = false;
	public EnumDiploType dipl_team = EnumDiploType.HOSTILES;
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
	public float prevHealth;
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
		
		setMoveSpeed(0.28F);
		
		fleeSpeed = 0.32F;
		
		entityCollisionReduction = 0.9F;
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
		setState(EnumActState.IDLE);
		
		
		//this.job.swapJob(job);
		//this could really be done in each unique entities init, or is this more of a template setup?
		this.job.clearJobs();
		if (job == EnumJob.HUNTER) {
			addJob(EnumJob.FINDFOOD);
	        addJob(EnumJob.HUNTER);
	        addJob(EnumJob.MUSIC);
		} else if (job == EnumJob.FISHERMAN) {
			addJob(EnumJob.FINDFOOD);
	        addJob(EnumJob.FISHERMAN);
	        addJob(EnumJob.MUSIC);
		} else if (job == EnumJob.TRADING) {
			addJob(EnumJob.FINDFOOD);
			addJob(EnumJob.TRADING);
	        addJob(EnumJob.HUNTER);
		} else if (job == EnumJob.GATHERER) {
			addJob(EnumJob.GATHERER);
		} else if (job == EnumJob.INVADER) {
			addJob(EnumJob.INVADER);
		} else if (job == EnumJob.PROTECT) {
			addJob(EnumJob.PROTECT);
			addJob(EnumJob.HUNTER);
		} else {
			addJob(EnumJob.UNEMPLOYED);
		}
		
		if (initItems) {
			
			if (fakePlayer == null) {
                fakePlayer = newFakePlayer(worldObj);
            }
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
	
	@Override
	public boolean interact(EntityPlayer var1) {
    	return (job != null & job.getPrimaryJobClass() != null) ? job.getPrimaryJobClass().interact(var1) : false;
		//return job.getPrimaryJobClass().interact(var1);
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
	            			//System.out.println(((c_EnhAI) entity1).name + " alerted");
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
		
		//cleanup, if this isnt called server side when the entity is dead, memory leak
		if (fakePlayer != null) {
			c_CoroAIUtil.playerToAILookup.remove(fakePlayer.username);
			//System.out.println("removing instance: " + fakePlayer.username);
			//System.out.println("c_CoroAIUtil.playerToAILookup: " + c_CoroAIUtil.playerToAILookup.size());
		}
		
		if (PFQueue.pfDelays != null) PFQueue.pfDelays.remove(this);
		if (job != null) {
			job.clearJobs();
			job.jobTypes.clear();
			job.ent = null;
		}
		func_130011_c((EntityLivingBase)null);
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
            	if (targ instanceof EntityPlayer && ((EntityPlayer)targ).username.contains("fakePlayer")) {
            		((EntityCreature)entity1).setAttackTarget(null);
            		
            		//System.out.println("fakeplayer de-targeting broken");
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
	            	if (((EntityLivingBase) entity1).canEntityBeSeen(this)) {
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
		if (func_110143_aJ() < func_110138_aP() * 0.75) {
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
		return (float) this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111126_e();
	}
	
	public void setMoveSpeed(float var) {
		//moveSpeed = var;
		this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(var);
		oldMoveSpeed = var;
		if (!worldObj.isRemote) {
			this.dataWatcher.updateObject(23, Integer.valueOf((int)(var * 1000)));
		}
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
	
	            for (this.moveForward = this.getMoveSpeed(); var15 < -180.0F; var15 += 360.0F)
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
		
		//dynamic speed adjuster syncer
		if (worldObj.isRemote) {
			float speed = this.dataWatcher.getWatchableObjectInt(23) / 1000;
			
			if (this.getMoveSpeed() != speed) {
				this.setMoveSpeed(speed);
			}
		}
		
		//setDead();
		//super.onLivingUpdate();
		if (worldObj.isRemote) {
			if (dataWatcher.getWatchableObjectInt(22) == 1) motionY = 0F;
			//this.setCurrentSlot(this.dataWatcher.getWatchableObjectInt(22));
		} else {
			if (onGround) {
				dataWatcher.updateObject(22, Integer.valueOf(1));
			} else {
				dataWatcher.updateObject(22, Integer.valueOf(0));
			}
		}
		
		if (c_CoroAIUtil.isServer()) {
			//System.out.println("Server lunge: " + lungeFactor);
		} else {
			//System.out.println("Client lunge: " + lungeFactor);
		}
		
		float factor = lungeFactor;
		factor = lungeFactor;
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
		
		//client side listens and sets swingArm, server side resets after something made it swing
		if (worldObj.isRemote) {
			if (this.dataWatcher.getWatchableObjectInt(21) == 1) {
				if (c_CoroAIUtil.isServer()) {
					//System.out.println("Server swingArm: " + swingArm);
				} else {
					//System.out.println("Client swingArm: " + swingArm);
				}
				swingArm = true;
				//this.dataWatcher.updateObject(21, 0);
			}
		} else {
			if (this.dataWatcher.getWatchableObjectInt(21) == 1) {
				this.dataWatcher.updateObject(21, 0);
			}
		}
		
		//setAIMoveSpeed(0.24F);
		
		
		
		super.onLivingUpdate();
	}
	
	@Override

	/**
	 * Updates the arm swing progress counters and animation progress
	 */
	public void updateArmSwingProgress() {
		//do nothing, cancel out the entity living arm swing so ours works
	}
	
	@Override
	public void jump()
    {
		super.jump();
    }
	
	public void doMovement() {
		
		//setDead();
		
		if (this.deathTime > 0) return;
		
		//float factor = lungeFactor;
		
		//fix for last node being too high
		if (!worldObj.isRemote) {
			PathEntity pe = this.getNavigator().getPath();
			if (pe != null) {
				if (pe.getCurrentPathLength() == 1) {
					//if (job.priJob == EnumJob.TRADING) {
						if (pe.getFinalPathPoint().yCoord - posY > 0.1F) {
							//System.out.println(pe.getFinalPathPoint().yCoord - posY);
							this.getNavigator().clearPathEntity();
						}
					//}
				}
			}
		}
		
		//Lunge speed!
		if (fleeing) {
			//this.func_48098_g(moveSpeed);
			this.dataWatcher.updateObject(20, 1);
		
		} else if (entityToAttack != null && this.isSolidPath(entityToAttack))	{
			//if (moveForward == 0F) {
			//if (getNavigator().func_48670_c() == null) {
				//getNavigator().func_48670_c().func_48644_d();
				//getNavigator().clearPathEntity();
				//System.out.println("what?!");
			
				job.getJobClass().onCloseCombatTick();
			
				
		    	
				//this.moveSpeed = this.moveForward = 0.7F * lungeFactor;
				//System.out.println(this.moveSpeed);
				//this.getMoveHelper().func_48186_a()
			//}
			
		//}
		/*this.moveForward = this.moveSpeed;
		this.faceEntity(entityToAttack, 30, 30);
		this.moveEntityWithHeading(this.moveStrafing, this.moveForward * 0.1F);*/
		} else if (!this.getNavigator().noPath()) {
			this.dataWatcher.updateObject(20, 1);
			Vec3 vec = this.getNavigator().getPath().getPosition(this);
			//getLookHelper().setLookPosition(vec.xCoord, vec.yCoord+this.getEyeHeight(), vec.zCoord, 10.0F, (float)getVerticalFaceSpeed());
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
				
				if (this.isInWater()) {
					if (!this.getNavigator().noPath()) {
						if (Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ) > 0.001F && Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ) < 0.5F) {
							this.moveFlying(this.moveStrafing, this.moveForward, 0.04F);
							//this.motionX *= 1.3F;
							//this.motionZ *= 1.3F;
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
	
	public boolean isBreaking() {
		return false;
	}
	
	@Override
	protected void entityInit()
    {
		super.entityInit();
        this.dataWatcher.addObject(20, Integer.valueOf(0)); //Move speed state
        this.dataWatcher.addObject(21, Integer.valueOf(0)); //Swing arm state
        this.dataWatcher.addObject(22, Integer.valueOf(0)); //onGround state for fall through floor fix
        this.dataWatcher.addObject(23, Integer.valueOf(0)); //uhhh??
        //24 is used in baseentai
    }
	
	@Override
	public void swingItem()
    {
    	super.swingItem();
    	//if (serverMode) {
		//swingArm = true;
    	//fakePlayer.addExhaustion(0.14F);
    	//this.dataWatcher.updateObject(21, 1);
    	//}
    }
	
	public boolean canSeeBlock(int x, int y, int z) {
		MovingObjectPosition mop = this.worldObj.clip(Vec3.createVectorHelper(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ), Vec3.createVectorHelper(x, y, z));
		if (mop == null) return true;
		if (mop.blockX == x && mop.blockY == y && mop.blockZ == z) return true;
		return false;
	}
	
	public void updateAI() {
		
		if (isDead) return;
		
		//debug = false;
		//setState(EnumActState.IDLE);
		//temp overrides, test settings?
		//maxDistanceFromHome = 512F;
		//maxReach_Ranged = 12F;
		if (false) {
			setDead();
			return;
		}
		
		/*if (job.getPrimaryJobClass() instanceof JobTrade) {
			System.out.println("exec: " + this);
		}*/
		
		//this.setAttackTarget(null);
		//this.setEntityToAttack(null);
		//System.out.println(name);
		
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
		} else {
			entityToAttack = null;
		}
		
		double var12 = 1D;//var5.yCoord - (double)var21;
		
		if (var12 > 0.0D)
        {
            //this.isJumping = true;
        }
		
		if (this.isCollidedHorizontally && this.hasPath())
		{
		    //this.isJumping = true;
		    //if (onGround) jump();
		}
		
		
		
		
		
		
		
		//AI Timeouts
		if (pfTimeout > 0) { pfTimeout--; }
		if (groupInfoDelay > 0) groupInfoDelay--;
		if (dipl_spreadInfoDelay > 0) dipl_spreadInfoDelay--;
		if (jumpDelay > 0) jumpDelay--;
		
		//Base class free vanilla mob ai awareness increasing
		if (enhancedAIDelay-- <= 0) {
			enhancedAIDelay = 100 + rand.nextInt(50);
			if (enhanceAIEnemies) Behaviors.enhanceMonsterAI(this);
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
		
		
		debugInfo = new StringBuilder().append(oldName + ": " + func_110143_aJ() + "|" + getFoodLevel() + " | " + job.priJob + " -> " + job.getJobState() + "|" + currentAction + "|" + pfNodes + "|").toString();
		//name = debugInfo;
		//System.out.println(debugInfo);
		
		if (debug) {
			/*name = new StringBuilder().append(oldName + ": " + currentAction + " | " + health
					+ " | " + fakePlayer.foodStats.getFoodLevel() + " | " + fakePlayer.foodStats.getFoodSaturationLevel()
					+ " | " + occupation + " -> " + occupationState + " - " + walkingTimeout + "|" + (Integer)Behaviors.getData(this, DataTypes.noMoveTicks)
					+ "|" + this.facingWater + "|" + pfNodes).toString();*/
			
			
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
			setMoveSpeed(oldMoveSpeed);
			
		//Enemy detected? (by alert system?)
		} else if (dangerLevel == 1) {
			//no change for now
			updateJob();
			setMoveSpeed(oldMoveSpeed);
			
		//Low health, avoid death
		} else if (dangerLevel == 2) {
			
			//If nothing to avoid
			if (!job.getJobClass().avoid(true)) {
				fleeing = false;
				//no danger in area, try to continue job
				updateJob();
				setMoveSpeed(oldMoveSpeed);
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
				
				
				
				setMoveSpeed(fleeSpeed);
			}
		}
		
		/*if (job.getJob() == EnumJob.TRADING) {
			int wweaeaweawe = 0;
			System.out.println(job.getJobState());
		}*/
		
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
				
				job.getPrimaryJobClass().transferItems(chest, inventory, id, 1, food);
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
		if (this.getDistance(targX, targY, targZ) < 2F || getNavigator().getPath() == null || getNavigator().getPath().isFinished()) {
			this.setPathToEntity((PathEntityEx)null);
			getNavigator().clearPathEntity();
			setState(EnumActState.IDLE);
		} else if (job.getJobClass().walkingTimeout <= 0) {
			this.setPathToEntity((PathEntityEx)null);
			getNavigator().clearPathEntity();
			setState(EnumActState.IDLE);
		}
	}
	
	//old code, remake entirely
	public void actFollow() {
		//temp
		EntityLivingBase entityplayer = null;
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
	                	if (wantedItems.contains(ent.getEntityItem().getItem().itemID)) {
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
        	
        	//System.out.println("nbt loaded c_EnhAI id: " + entID);
        	
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
	        
	        if (job.getJobClass() != null) var1.setInteger("tradeTimeout", job.getJobClass().tradeTimeout);
	        
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
		
		if (job != null && job.getJobClass() != null) job.getJobClass().hitHook(damagesource, i);
		
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
	
	//public boolean pathfindLocked = false;
	public boolean waitingForNewPath = false;
	
	public void checkPathfindLock() {
		if (waitingForNewPath) {
			getNavigator().setPath(PFQueue.instance.convertToPathEntity(pathToEntity), getMoveSpeed());
			waitingForNewPath = false;
		}
	}
	
	@Override
	public synchronized void setPathExToEntity(PathEntityEx pathentity)
    {
        pathToEntity = pathentity;
        waitingForNewPath = true;
    }
	
	@Override
	public int overrideBlockPathOffset(c_IEnhAI ent, int id, int meta, int x, int y, int z) {
		// TODO Auto-generated method stub
		return -66;
	}
}
