package CoroUtil.bt;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import CoroUtil.bt.actions.Delay;
import CoroUtil.bt.actions.OrdersUser;
import CoroUtil.bt.nodes.SelectorMoveToPathBest;
import CoroUtil.bt.nodes.SelectorMoveToPathClose;
import CoroUtil.bt.nodes.SelectorMoveToPosVec;
import CoroUtil.bt.nodes.SenseEnvironment;
import CoroUtil.bt.selector.Selector;
import CoroUtil.bt.selector.SelectorBoolean;
import CoroUtil.bt.selector.SelectorConcurrent;
import CoroUtil.bt.selector.SelectorSequence;
import CoroUtil.diplomacy.DiplomacyHelper;
import CoroUtil.diplomacy.TeamInstance;
import CoroUtil.diplomacy.TeamTypes;
import CoroUtil.pathfinding.PFQueue;


public class AIBTAgent {

	//most generic configs go here
	
	//Blackboard obj with:
	//active moveto vec
	//active closerange instant pf
	//active longrange pf
	//target (by id for threading?)
	
	public AIEventHandler eventHandler;
	public PersonalityProfile profile;
	public TeamInstance dipl_info = TeamTypes.getType("neutral");
	public BlackboardBase blackboard;
	public IBTAgent entInt;
	public EntityLivingBase ent;

  	public OrdersHandler ordersHandler;
	
  	public AIBTTamable tamable;
  	
	public Selector btSenses;
	public Selector btAI;
	public Selector btMovement;
	public Selector btAttack;
	
	public PathNavigateCustom pathNav;
	public EntityMoveHelperCustom moveHelper;
	
	//settings, move?
	public float moveSpeed = 0.6F;
	public boolean canDespawn = false;
	public String acceptableOrders = "gather build guard_position";
	
	//runtime
	private boolean hasInit = false;
	public int tickAge = 0;
	public int tickDespawn = 0;
	//private boolean tickMove = false;
  	
  	//animation help, needs refactor
  	public long lastAnimateUpdateTime = 0;
  	
  	public static boolean DEBUGTREES = false;
	
	public AIBTAgent(IBTAgent parEnt) {
		ent = parEnt.getEntityLiving();
		entInt = parEnt;
		ordersHandler = new OrdersHandler(parEnt);
		blackboard = new BlackboardBase(this);
		eventHandler = new AIEventHandler(this);
		tamable = new AIBTTamable(this);
		profile = new PersonalityProfile(this);
		profile.init();
		pathNav = new PathNavigateCustom(ent, ent.worldObj);
		pathNav.setAvoidsWater(false);
		pathNav.setCanSwim(true);
		moveHelper = new EntityMoveHelperCustom(ent);
		
		ent.entityCollisionReduction = 0.2F;
	}
	
	public void entityInit()
    {
		//IDS USED ELSEWHERE:
		//27 is used in baseentai
		
		//Checked for 1.6.4:
		//Living ends at 10
		//Agable uses 12
		
        //this.dataWatcher.addObject(20, Integer.valueOf(0)); //Move speed state
        //this.dataWatcher.addObject(21, Integer.valueOf(0)); //Swing arm state
		
        ent.getDataWatcher().addObject(22, Integer.valueOf(0)); //onGround state for fall through floor fix
        //ent.getDataWatcher().addObject(23, new Integer(ent.getMaxHealth()));
        ent.getDataWatcher().addObject(24, Integer.valueOf(0)); //AI state, used for stuff like sitting animation, etc
        ent.getDataWatcher().addObject(25, Integer.valueOf(0)); //swing arm state
        
    }
	
	public void initBTTemplate() {
		
		this.btSenses = new SelectorConcurrent(null);
        this.btSenses.add(new SenseEnvironment(this.btSenses, this.blackboard));
        
        
		//General AI template
        
        //doSurvive and doIdle are profiled
        
        //down = false, up = true
        
        /*
         *                    doOrders
         *      shouldOrders<                doSurvive (flee, call for help, nothing, etc)
         * top<               shouldSurvive<             
         *                                   isFighting< <- we need a profile here for fighting, so fleeers dont fight
         *                                               doIdle (Idle wander, personal hunting)
         */
        
        Delay delay = new Delay(null, 0, 0);
        /*Selector selSurvivalPerform = new SelectorConcurrent(null);
        selSurvivalPerform.add(new Flee(selSurvivalPerform, entInt, blackboard));*/
        
        Selector isFighting = new SelectorBoolean(null, blackboard.isFighting);
        isFighting.debug = "isFighting";
        isFighting.add(profile.btIdling);
        isFighting.add(delay);
        //isFighting.add(new TrackTarget(null, entInt, blackboard));
        
        //this.btMovement.add(isFighting);
        
		Selector selOrdersPerform = new SelectorSequence(null);
		selOrdersPerform.debug = "selOrdersPerform";
		selOrdersPerform.add(new OrdersUser(selOrdersPerform, ordersHandler, getAcceptableOrders()));

		Selector shouldFollowOrders = new SelectorBoolean(null, blackboard.shouldFollowOrders);
		shouldFollowOrders.debug = "shouldFollowOrders";
		
		Selector shouldSurvive = new SelectorBoolean(null, blackboard.shouldTrySurvival);
		shouldSurvive.debug = "shouldSurvive";
		
		shouldFollowOrders.add(shouldSurvive);
		shouldFollowOrders.add(selOrdersPerform);
		
		shouldSurvive.add(isFighting);
		shouldSurvive.add(profile.btSurviving);
		
		btAI = new SelectorConcurrent(null);
		btAI.add(shouldFollowOrders);
		
		
        
        //Movement template, doesnt have link to profile
        
        SelectorBoolean selLongPath = new SelectorBoolean(this.btMovement, this.blackboard.isLongPath);
        
        SelectorMoveToPathBest sel1_PathBest = new SelectorMoveToPathBest(selLongPath, this.entInt, this.blackboard);
        SelectorBoolean selSafeOrClosePath = new SelectorBoolean(selLongPath, this.blackboard.isSafeOrClosePath);
        
        
        SelectorMoveToPathClose sel_PathClosePartial = new SelectorMoveToPathClose(null, this.entInt, this.blackboard, 1, true);
        SelectorMoveToPathClose sel_PathCloseExact = new SelectorMoveToPathClose(null, this.entInt, this.blackboard, 1, false);
        SelectorMoveToPosVec sel_MoveToPos = new SelectorMoveToPosVec(selSafeOrClosePath, this.entInt, this.blackboard, 2F);
        
        selLongPath.add(selSafeOrClosePath);
        selLongPath.add(sel1_PathBest);
        
        //sel1_PathBest.add(sel_MoveToPos); //temp test since temp insta pf having issues
        sel1_PathBest.add(sel_PathClosePartial); //change to partial once its coded
        sel1_PathBest.add(delay);
        selSafeOrClosePath.add(sel_PathCloseExact);
        selSafeOrClosePath.add(sel_MoveToPos);
        
        sel_MoveToPos.add(delay);
        sel_MoveToPos.add(delay);
        
        this.btMovement = new SelectorConcurrent(null);
        this.btMovement.add(selLongPath);
        
        
        
        //Attack template
        
        Selector shouldAttackPerform = new SelectorBoolean(null, blackboard.isFighting);
		shouldAttackPerform.add(new Delay(null, 0, 0));
		shouldAttackPerform.add(profile.btAttacking);
        
        this.btAttack = new SelectorConcurrent(null);
		btAttack.add(shouldAttackPerform);
	}
	
    public String getAcceptableOrders() {
    	return acceptableOrders;
    }
	
	public void setSpeedNormalBase(float var) {
		moveSpeed = var;
	}
	
	public void applyEntityAttributes() {
		//baseline movespeed
		ent.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(moveSpeed);
	}
	
	public void tickAI() {
		
		if (ent.worldObj == null || ent.worldObj.provider == null) return;
		
		if (btSenses != null) btSenses.tick();
		if (btAI != null) btAI.tick();
		if (btMovement != null) btMovement.tick();
		if (btAttack != null) btAttack.tick();
		
		//THIS IS TEMP AND TO BE MOVED/USED DIFFERENTLY, AKA MOVE THE TELEPORTER CODE TO A NODE
		tamable.tick();
		
		tickMovement();
		tickAgeEntity();
		
		ent.entityCollisionReduction = 0.2F;
	}
	
	public void tickMovement() {
		
		//for pathfind based movement
		//needs conditional
		//ent.getNavigator().onUpdateNavigation();
		double entSpeed = Math.sqrt(ent.motionX + ent.motionX * ent.motionY + ent.motionY * ent.motionZ + ent.motionZ);
		
		//help!
		if (ent.isInWater()) {
			//bah!
			//ent.setDead();
			Random rand = new Random();
			if (entSpeed < 0.5F) {
				ent.motionX *= 1.4F;
				ent.motionZ *= 1.4F;
			}
			ent.motionY += 0.25F;
			/*ent.motionX += rand.nextDouble() - rand.nextDouble();
			ent.motionY += 0.6F;
			ent.motionZ += rand.nextDouble() - rand.nextDouble();*/
		}
		
		double speed = 0.2D;
		int id = ent.worldObj.getBlockId(MathHelper.floor_double(ent.posX), (int)ent.boundingBox.minY, MathHelper.floor_double(ent.posZ));
		if (id != 0 && Block.blocksList[id] instanceof BlockFence) {
			Random rand = new Random();
			ent.motionX += rand.nextDouble()*speed - rand.nextDouble()*speed;
			ent.motionY = 0.2F;
			ent.motionZ += rand.nextDouble()*speed - rand.nextDouble()*speed;
			blackboard.posMoveTo = null;
		} else {
			id = ent.worldObj.getBlockId(MathHelper.floor_double(ent.posX), (int)ent.boundingBox.minY-1, MathHelper.floor_double(ent.posZ));
			if (id != 0 && Block.blocksList[id] instanceof BlockFence) {
				Random rand = new Random();
				ent.motionX += rand.nextDouble()*speed - rand.nextDouble()*speed;
				ent.motionY = 0.2F;
				ent.motionZ += rand.nextDouble()*speed - rand.nextDouble()*speed;
				blackboard.posMoveTo = null;
			}
		}
		
		//help pathing, fix this move it or something
		if (ent.onGround && ent.isCollidedHorizontally) {
			//if (ent.motionY < 0.5F) ent.motionY += 0.5F;
		}
		
		//main mc movement class calls
		moveHelper.onUpdateMoveHelper();
		
		//only runs on true AI entities, patched for potential client player usage
		if (ent instanceof EntityLiving) {
			((EntityLiving)ent).getLookHelper().onUpdateLook();
			((EntityLiving)ent).getJumpHelper().doJump();
		}
	}
	
	public void setMoveTo(double par1, double par3, double par5)
    {
		/*blackboard.posMoveTo.xCoord = par1;
		blackboard.posMoveTo.yCoord = par1;
		blackboard.posMoveTo.zCoord = par1;*/
		moveHelper.setMoveTo(par1, par3, par5, moveSpeed);
    }
	
	public void tickLiving() {
        if (profile.abilities.size() > 0) {
        	/*if (ent.worldObj.isRemote) {
        		System.out.println("SDfsdfsdf");
        	}*/
			profile.tickAbilities();
		}
	}
	
	public boolean isEnemy(Entity ent) {
		//if (ent instanceof EntityEpochBase) return true;
		//return false;
		return DiplomacyHelper.shouldTargetEnt(this.entInt, ent);
	}
	
	
	
	public void initPost(boolean fromDisk) {
		hasInit = true;
		
		
		postFullInit();
	}
	
	public void postFullInit() {
		profile.updateListCache();
		profile.syncAbilitiesFull(true); //calling this here does not work for entities outside tracker range on client, see SkillMapping errors for more detail
	}
	
	public EntityLivingData onSpawnEvent(EntityLivingData par1EntityLivingData) {
		initPost(false);
		return par1EntityLivingData;
	}
	
    public void nbtRead(NBTTagCompound par1nbtTagCompound) {
    	tickAge = par1nbtTagCompound.getInteger("tickAge");
		canDespawn = par1nbtTagCompound.getBoolean("canDespawn");
    	
    	profile.nbtRead(par1nbtTagCompound);
    	tamable.setTamedByOwner(par1nbtTagCompound.getString("owner"));
    	initPost(true);
	}
	
    public void nbtWrite(NBTTagCompound par1nbtTagCompound) {
    	par1nbtTagCompound.setInteger("tickAge", tickAge);
    	par1nbtTagCompound.setBoolean("canDespawn", canDespawn);
    	
    	profile.nbtWrite(par1nbtTagCompound);
    	par1nbtTagCompound.setString("owner", tamable.owner);
	}
    
    public void nbtDataFromServer(NBTTagCompound nbt) {
		String command = nbt.getString("command");
		
		if (command.equals("syncAbilities")) {
			profile.nbtSyncRead(nbt);
		}
	}
    
    public void tickAgeEntity()
    {

    	tickAge++;

    	if (canDespawn) {

			//reset respawn chance if pathing, otherwise tick
			if (pathNav.noPath() || blackboard.getTarget() == null) {
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
    
    public void cleanup() {
    	PFQueue.pfDelays.remove(ent);
		ent = null;
		ordersHandler = null;
		eventHandler.cleanup();
		entInt.cleanup();
		entInt = null;
    }
}
