package CoroAI.componentAI.jobSystem;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import CoroAI.PFQueue;
import CoroAI.c_CoroAIUtil;
import CoroAI.componentAI.AIAgent;
import CoroAI.componentAI.AITamable;
import CoroAI.componentAI.ICoroAI;
import CoroAI.entity.EnumActState;
import CoroAI.entity.EnumJobState;
import CoroAI.util.CoroUtilInventory;

public class JobBase {
	
	public JobManager jm = null;
	public AIAgent ai = null;
	public EntityLiving ent = null;
	public ICoroAI entInt = null;
	
	public EnumJobState state;
	
	//Shared job vars
	public int hitAndRunDelay = 0;
	public int tradeTimeout = 0;
	public int walkingTimeout;
	
	public int fleeDelay = 0;
	public boolean dontStrayFromHome = false;
	public boolean dontStrayFromOwner = true;
	
	public double targPrevPosX;
	public double targPrevPosY;
	public double targPrevPosZ;
	
	public int ticksBeforeCloseCombatRetry = 0;
	public int ticksBeforeFormationRetry = 0;
	
	public long itemLookDelay;
	public int itemSearchRange = 10;
	
	//Taming fields
	public AITamable tamable;
	
	public JobBase(JobManager parJm) {
		this.jm = parJm;
		this.ai = jm.ai;
		this.ent = jm.ai.ent;
		this.entInt = jm.ai.entInt;
		this.tamable = new AITamable(this);
		//this.ent = (EntityLivingBase)jm.ent;
		setJobState(EnumJobState.IDLE);
	}
	
	public void cleanup() {
		tamable.cleanup();
		tamable = null;
		entInt = null;
		ent = null;
		ai = null;
		jm = null;
	}
	
	public void setJobState(EnumJobState ekos) {
		state = ekos;
		//System.out.println("jobState: " + occupationState);
	}

	public void tick() {
		if (hitAndRunDelay > 0) hitAndRunDelay--;
		if (tradeTimeout > 0) tradeTimeout--;
		tamable.tick();
	}
	
	public boolean shouldExecute() {
		return true;
	}
	
	public boolean shouldContinue() {
		return true;
	}
	
	public void onTickChestScan() {
		if (ai.scanForHomeChest && ent.worldObj.getWorldTime() % 100 == 0) {
			if (!CoroUtilInventory.isChest(ent.worldObj.getBlockId(ai.homeX, ai.homeY, ai.homeZ))) {
				//System.out.println("scanning for chests or allies - " + ent);
				ChunkCoordinates tryCoords = getChestNearby();
				if (tryCoords != null) {
					//System.out.println("discovered a chest to call home! - " + ent);
					ai.homeX = tryCoords.posX;
					ai.homeY = tryCoords.posY;
					ai.homeZ = tryCoords.posZ;
				} else {
					int range = 30;
					List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(range, range/2, range));
			        for(int j = 0; j < list.size(); j++)
			        {
			            Entity entity1 = (Entity)list.get(j);
			            
			            if (entity1 instanceof ICoroAI) {
			            	if (CoroUtilInventory.isChest(ent.worldObj.getBlockId(((ICoroAI) entity1).getAIAgent().homeX, ((ICoroAI) entity1).getAIAgent().homeY, ((ICoroAI) entity1).getAIAgent().homeZ))) {
			            		ai.homeX = ((ICoroAI) entity1).getAIAgent().homeX;
			            		ai.homeY = ((ICoroAI) entity1).getAIAgent().homeY;
			            		ai.homeZ = ((ICoroAI) entity1).getAIAgent().homeZ;
			            		//System.out.println("discovered a friend with a chest to call home! - " + ent);
			            		return;
			            	}
			            }
			        }
				}
			}
		}
	}
	
	public ChunkCoordinates getChestNearby() {
		
		int range = 30;
		
		for (int xx = (int)Math.floor(ent.posX - range/2); xx < ent.posX + range/2; xx++) {
			for (int yy = (int)Math.max(1, Math.floor(ent.posY - 2)); yy < ent.posY + 2; yy++) {
				for (int zz = (int)Math.floor(ent.posZ - range/2); zz < ent.posZ + range/2; zz++) {
					int id = ent.worldObj.getBlockId(xx, yy, zz);
					
					if (CoroUtilInventory.isChest(id)) {
						return new ChunkCoordinates(xx, yy, zz);
					}
				}
			}
		}
		return null;
	}
	
	public void onLowHealth() {
		if (hitAndRunDelay > 0) hitAndRunDelay--;
		PathEntity pe = ent.getNavigator().getPath();
		
		if (pe != null && !pe.isFinished()) {
			
			if (ent.worldObj.clip(pe.getPosition(ent), Vec3.createVectorHelper(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ)) == null) {
				if (pe.getPosition(ent).distanceTo(Vec3.createVectorHelper(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ)) < 3F) {
					pe.incrementPathIndex();
				}
				//System.out.println("next path!");
			}
			
			/*int pIndex = pe.pathIndex+1;
			if (pIndex < this.pathToEntity.points.length) {
				if (this.worldObj.rayTraceBlocks(Vec3.createVectorHelper((double)pathToEntity.points[pIndex].xCoord + 0.5D, (double)pathToEntity.points[pIndex].yCoord + 1.5D, (double)pathToEntity.points[pIndex].zCoord + 0.5D), Vec3.createVectorHelper(posX, posY + (double)getEyeHeight(), posZ)) == null) {
					this.pathToEntity.pathIndex++;
				}
			}*/
		}
		
		if (ent.onGround && ent.isCollidedHorizontally && !entInt.isBreaking()) {
    		c_CoroAIUtil.jump(ent);
		}
	}
	
	public void onIdleTick() {
		if (tamable.isTame()) {
			tamable.onIdleTick();
		} else {
			onIdleTickAct();
		}
	}
	
	public void onIdleTickAct() {
		
		if (isInFormation() && ai.activeFormation.leader != entInt) {
			return;
		}
		
        if(((ent.getNavigator().noPath()) && ai.rand.nextInt(120) == 0))
        {
        	if (!dontStrayFromHome) {
        		ai.updateWanderPath();
        	} else {
        	//System.out.println("home dist: " + ent.getDistance(ent.homeX, ent.homeY, ent.homeZ));
	        	if (ent.getDistance(ai.homeX, ai.homeY, ai.homeZ) < ai.maxDistanceFromHome) {
	        		if (ai.rand.nextInt(5) == 0) {
	        			int randsize = 32;
	            		ai.walkTo(ent, ai.homeX+ai.rand.nextInt(randsize) - (randsize/2), ai.homeY+1, ai.homeZ+ai.rand.nextInt(randsize) - (randsize/2),ai.maxPFRange, 600);
	        		} else {
	        			ai.updateWanderPath();
	        		}
	        	} else {
	        		int randsize = 32;
	        		ai.walkTo(ent, ai.homeX+ai.rand.nextInt(randsize) - (randsize/2), ai.homeY+1, ai.homeZ+ai.rand.nextInt(randsize) - (randsize/2),ai.maxPFRange, 600);
	        	}
        	}
        } else {
        	if (ent.getNavigator().noPath()) {
    			if (ai.useInv && ai.entInv.shouldLookForPickups) lookForItems();
        	}
        }
	}
	
    public void lookForItems() {
    	
    	itemSearchRange = 10;
    	if (itemLookDelay < System.currentTimeMillis()) {
    		itemLookDelay = System.currentTimeMillis() + 500;
    	
	    	List var3 = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(itemSearchRange*1.0D, itemSearchRange*1.0D, itemSearchRange*1.0D));
	
	        if(var3 != null) {
	            for(int var4 = 0; var4 < var3.size(); ++var4) {
	                Entity var5 = (Entity)var3.get(var4);
	
	                if(!var5.isDead && var5 instanceof EntityItem) {
	                	EntityItem entTemp = (EntityItem)var5;
	                	if (ai.entInv.wantedItems.contains(entTemp.getEntityItem().getItem().itemID)) {
		                	if (this.ent.canEntityBeSeen(var5)) {
		                		//if (this.team == 1) {
		                		if (!var5.isInsideOfMaterial(Material.water)) {
		                			//targetItem(var5);
		                			PFQueue.getPath(ent, var5, itemSearchRange+2F);
		                		}
		                	}
	                	}
	                } else if (var5 instanceof EntityXPOrb) {
	                	if (ent.canEntityBeSeen(var5)) {
	                		if (!var5.isInsideOfMaterial(Material.water)) {
	                			//targetItem(var5);
	                			PFQueue.getPath(ent, var5, itemSearchRange+2F);
	                		}
	                	}
	                }
	            }
	        }
    	}
    }
	
	public void onJobRemove() {
		//Job cleanup stuff - 
		if (ai.useInv) this.ai.entInv.setCurrentSlot(0);
	}
	
	public void setJobItems() {
		
	}
	
	// Blank functions \\
		
	public boolean sanityCheck(Entity target) {
		return false;
	}
	
	public boolean sanityCheckHelp(Entity caller, Entity target) {
		return false;
	}
	
	/* return false to cancel processing */
	public boolean hookHit(DamageSource ds, int damage) {
		return true;
	}
	
	/* return false to cancel processing */
	public boolean hookInteract(EntityPlayer par1EntityPlayer) {
		return true;
	}
	
	// Blank functions //
	
	// Job shared functions \\
	
	public boolean checkHunger() {
		//System.out.println("TEMP OFF FOR REFACTOR");
		
		if (!ai.useInv || ai.entInv.fakePlayer == null) return false;
		
		if (ai.entInv.fakePlayer.getFoodStats().getFoodLevel() <= 16) {
			if (ai.entInv.eat()) {
				//System.out.println("NH: " + fakePlayer.foodStats.getFoodLevel());
			} else {
				//fallback();
				//if (jm.getJob() != EnumJob.FINDFOOD) {
					//ent.swapJob(EnumJob.FINDFOOD);
					return true;
				//}
			}
			//try heal
		}
		return false;
	}
	
	public boolean checkDangers() {
		return checkHealth();
	}
	
	public boolean checkHealth() {
		if (ent.func_110143_aJ() < ent.func_110138_aP() * 0.75) {
			return true;
		}
		return false;
	}
	
	public boolean avoid(boolean actOnTrue) {
		Entity clEnt = null;
		float closest = 9999F;
		
		if (ai.lastFleeEnt != null && ai.lastFleeEnt.isDead) { ai.lastFleeEnt = null; }
		if (fleeDelay > 0) fleeDelay--;
		
		float range = 15F;
		
            {
            	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(range, range/2, range));
            	for(int j = 0; j < list.size(); j++)
            	{
            		Entity entity1 = (Entity)list.get(j);
            		if(!entity1.isDead && isEnemy(entity1))
            	if (((EntityLivingBase) entity1).canEntityBeSeen(ent)) {
            		//if (sanityCheck(entity1)) {
            			float dist = ent.getDistanceToEntity(entity1);
            			if (dist < closest) {
            				closest = dist;
            				clEnt = entity1;
            			}
	            		
	            		//found = true;
	            		//break;
            		//}
            		//this.hasAttacked = true;
            		//getPathOrWalkableBlock(entity1, 16F);
            	}
            }
        }
        PathEntity path = ent.getNavigator().getPath();
        //System.out.println("koa " + ent.name + " health: " + ent.getHealth());
        if (clEnt != null) {
        	if (clEnt != ai.lastFleeEnt || (ent.getNavigator().noPath())) {
        		ai.lastFleeEnt = clEnt;
        		if (actOnTrue && fleeDelay <= 0) fleeFrom(clEnt);
        	}
        } else if (/*(ent.getNavigator().noPath()) && */ai.lastFleeEnt != null) {
    		if (actOnTrue && fleeDelay <= 0) fleeFrom(ai.lastFleeEnt);
        }
        
        //no idle wander for now
		if (ai.lastFleeEnt != null) {
			if (ai.lastFleeEnt.isDead) { ai.lastFleeEnt = null; }
			if (this.jm.priJob instanceof JobHunt) setJobState(EnumJobState.W1);
		} else {
			//setJobState(EnumJobState.IDLE);
		}
        
        if (clEnt != null) return true;
        return false;
	}
	
	public void fleeFrom(Entity fleeFrom) {
		
		fleeDelay = 10;
		/*this.faceEntity(fleeFrom, 180F, 180F);
		//this.rotationYaw += 180;
		
		double d1 = posX - fleeFrom.posX;
        double d2 = posZ - fleeFrom.posZ;
        float f2 = (float)((Math.atan2(d2, d1) * 180D) / 3.1415927410125732D) - 90F;
        float f3 = f2 - rotationYaw;
        
        rotationYaw = updateRotation2(rotationYaw, f3, 360F);*/
		
		double d = fleeFrom.posX - ent.posX;
        double d1;
        for (d1 = fleeFrom.posZ - ent.posZ; d * d + d1 * d1 < 0.0001D; d1 = (Math.random() - Math.random()) * 0.01D)
        {
            d = (Math.random() - Math.random()) * 0.01D;
        }
        float f = MathHelper.sqrt_double(d * d + d1 * d1);

        //knockBack(entity, i, d, d1);
        
        float yaw = (float)((Math.atan2(d1, d) * 180D) / 3.1415927410125732D) - ent.rotationYaw;;
		
		float look = ai.rand.nextInt(8)-4;
        //int height = 10;
        double dist = ai.rand.nextInt(8)+8;
        int gatherX = (int)(ent.posX + ((double)(-Math.sin((yaw+look) / 180.0F * 3.1415927F) * Math.cos(ent.rotationPitch / 180.0F * 3.1415927F)) * dist));
        int gatherY = (int)(ent.posY-0.5 + (double)(-MathHelper.sin(ent.rotationPitch / 180.0F * 3.1415927F) * dist) - 0D); //center.posY - 0D;
        int gatherZ = (int)(ent.posZ + ((double)(Math.cos((yaw+look) / 180.0F * 3.1415927F) * Math.cos(ent.rotationPitch / 180.0F * 3.1415927F)) * dist));
        
        gatherX = (int)(ent.posX - (d / f * dist));
        gatherZ = (int)(ent.posZ - (d1 / f * dist));
        
        int id = ent.worldObj.getBlockId(gatherX, gatherY, gatherZ);
        
        int offset = -10;
        
        while (offset < 10) {
        	if (id == 0) {
        		break;
        	}
        	
        	id = ent.worldObj.getBlockId(gatherX, gatherY+offset++, gatherZ);
        }
        
        double homeDist = ent.getDistance(ai.homeX, ai.homeY, ai.homeZ);
        
        //System.out.println("TEMP OFF FOR REFACTOR");
        
        ai.walkTo(ent, gatherX, gatherY, gatherZ, ai.maxPFRange, 600, -1);
        
        /*if (false && ai.jobMan.getJobClass() instanceof JobHunt && homeDist > ai.maxDistanceFromHome / 4 * 3) {
        	ai.walkTo(ent, ai.homeX, ai.homeY, ai.homeZ, (float) homeDist, 600);
        } else {
        	if (offset < 100) {
        		ai.walkTo(ent, gatherX, gatherY, gatherZ, ai.maxPFRange, 600, -1);
        	} else {
        		//System.out.println("flee failed");
            	ai.walkTo(ent, ai.homeX, ai.homeY, ai.homeZ, (float) homeDist, 600);
        	}
        }*/
	}
	
	public boolean findWater() {
		
		int scanSize = ai.maxPFRange;
		int scanSizeY = 60;
		
		int tryX;// = ((int)this.posX) + rand.nextInt(scanSize)-scanSize/2;
		int tryY = ((int)ent.posY) - 1;
		int tryZ;// = ((int)this.posZ) + rand.nextInt(scanSize)-scanSize/2;
		
		int i = tryY + ai.rand.nextInt(scanSizeY)-scanSizeY/2;
		
		//System.out.println(tryX + " " + i + " " + tryZ);
		for (int ii = 0; ii <= 5; ii++) {
			tryX = ((int)ent.posX) + ai.rand.nextInt(scanSize)-scanSize/2;
			i = tryY + ai.rand.nextInt(scanSizeY)-scanSizeY/2;
			tryZ = ((int)ent.posZ) + ai.rand.nextInt(scanSize)-scanSize/2;
			if (ent.worldObj.getBlockId(tryX, i, tryZ) == Block.waterStill.blockID || (Block.blocksList[ent.worldObj.getBlockId(tryX, i, tryZ)] != null && Block.blocksList[ent.worldObj.getBlockId(tryX, i, tryZ)].blockMaterial == Material.water)) {
				//System.out.println("found water");
				
				int newY = i;
				
				while (ent.worldObj.getBlockId(tryX, newY, tryZ) != 0) {
					newY++;
				}
				
				PFQueue.getPath(ent, tryX, newY-1, tryZ, scanSize/2+6);
				//this.setPathToEntity(worldObj.getEntityPathToXYZ(this, tryX, newY-1, tryZ, scanSize/2+6));
				
				//POST PATHFIND PATH LIMITER CODE GOES HERE
				//scan through pathnodes, look for where it goes from sand to water
				
				
				//if (!this.hasPath()) { System.out.println("no path"); }
				
				ai.setState(EnumActState.WALKING);
				walkingTimeout = 300;
				ai.targX = tryX;
				ai.targY = tryY;
				ai.targZ = tryZ;
				//System.out.println(tryX + " " + i + " " + tryZ);
				return true;
				
			} else {
				//System.out.println("no water");
			}
		}
		
		return false;
	}
	
	public boolean findLand() {
		
		int scanSize = 64;
		
		int tryX = ((int)ent.posX) + ai.rand.nextInt(scanSize)-scanSize/2;
		int tryY = ((int)ent.posY) + 5;
		int tryZ = ((int)ent.posZ) + ai.rand.nextInt(scanSize)-scanSize/2;
		
		//System.out.println(this.worldObj.getBlockId(tryX, tryY, tryZ));
		for (int i = tryY; i > tryY - 10; i--) {
			if (ent.worldObj.getBlockId(tryX, i, tryZ) != 0 && !((Block.blocksList[ent.worldObj.getBlockId(tryX, i, tryZ)] != null && Block.blocksList[ent.worldObj.getBlockId(tryX, i, tryZ)].blockMaterial == Material.water))) {
				//System.out.println("found water");
				
				PFQueue.getPath(ent, tryX, tryY, tryZ, scanSize/2+6);
				
				
				
				
				//if (!this.hasPath()) { System.out.println("no path"); }
				
				ai.setState(EnumActState.WALKING);
				walkingTimeout = 300;
				ai.targX = tryX;
				ai.targY = tryY;
				ai.targZ = tryZ;
				
				return true;
				
			} else {
				//System.out.println("no water");
			}
		}
		
		return false;
	}
	
	public EntityPlayer getClosestVulnerablePlayerToEntity(Entity par1Entity, double par2) {
		return getClosestPlayerToEntity(par1Entity, par2, true);
	}
	
	public EntityPlayer getClosestPlayerToEntity(Entity par1Entity, double par2, boolean survivalOnly)
    {
        return this.getClosestPlayer(par1Entity.worldObj, par1Entity.posX, par1Entity.posY, par1Entity.posZ, par2, survivalOnly);
    }
	
	public EntityPlayer getClosestPlayer(World world, double par1, double par3, double par5, double par7, boolean survivalOnly)
    {
        double var9 = -1.0D;
        EntityPlayer var11 = null;

        for (int var12 = 0; var12 < world.playerEntities.size(); ++var12)
        {
            EntityPlayer var13 = (EntityPlayer)world.playerEntities.get(var12);

            if ((!var13.capabilities.disableDamage || !survivalOnly) && var13.func_110143_aJ() > 0)
            {
                double var14 = var13.getDistanceSq(par1, par3, par5);

                if ((par7 < 0.0D || var14 < par7 * par7) && (var9 == -1.0D || var14 < var9))
                {
                    var9 = var14;
                    var11 = var13;
                }
            }
        }

        return var11;
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
            				//System.out.println("ourStack.stackSize < 0");
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
	
	public boolean shouldTickCloseCombat() {
		if (entInt.getAIAgent() == null) return false;
		Entity targ = entInt.getAIAgent().entityToAttack;
		if (targ == null) return false;
		
		//making close target code run if no path, with new close combat code this should be ok
		//if (ent.getNavigator().noPath() && !shouldTickFormation()) return true;
		
		//this if statement should perhaps be replaced with a better logic to determine when close combat should kick in again, path movement loops can occur with this timeout
		if (ticksBeforeCloseCombatRetry > 0) {
			ticksBeforeCloseCombatRetry--;
			return false;
		} else {
			return ent.canEntityBeSeen(targ) && (ent.getDistanceToEntity(targ) < 12.0F) && ent.boundingBox.minY - targ.boundingBox.minY <= 2.5D && ent.boundingBox.minY - targ.boundingBox.minY > -2.5D;
		}
	}
	
	public boolean isInFormation() {
		return ai.activeFormation != null;
	}
	
	public boolean shouldTickFormation() {
		
		//if (ent.isInWater()) return false;
		if (ai.entityToAttack != null) return false;// && (ent.getDistanceToEntity(ai.entityToAttack) > 20F || !ent.canEntityBeSeen(ai.entityToAttack))) return false;
		
		if (ticksBeforeFormationRetry > 0) {
			ticksBeforeFormationRetry--;
			return false;
		} else {
			return ai.activeFormation != null;
		}
	}
	
	public void onTickFormation() {
		
		//never true
		if (ai.activeFormation.leader == entInt) {
			//ent.motionX *= 0.5D;
			//ent.motionZ *= 0.5D;
			return;
		}
		
		Vec3 vec = ai.activeFormation.getPosition(ent);
		
		if (ent.isCollidedHorizontally && ent.onGround) ent.jump();
		
		if (ent.isInWater() && ent.getNavigator().noPath()) {
			//ent.motionY += 0.07D;
			//ent.motionX += 0.8D;
			if (vec != null) {
				if (Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ) < 0.1D) {
					double waterSpeed = 0.1D;
					ent.motionX -= Math.cos((-ent.rotationYaw + 90D) * 0.01745329D) * waterSpeed;
					ent.motionZ += Math.sin((-ent.rotationYaw + 90D) * 0.01745329D) * waterSpeed;
					//ent.motionX *= 1.3D;
					//ent.motionZ *= 1.3D;
				}
			}
		}
		
		float closeFactor = 1F;
		if (!ent.onGround) closeFactor = 0.1F;
		
		float speed = c_CoroAIUtil.getMoveSpeed(ent) * ai.lungeFactor * closeFactor;
		
		//System.out.println(ai.activeFormation.listEntities.size());
		
		if (isMovementSafe()) {
			if (vec != null) {
				if (ent.getDistance(vec.xCoord, ent.posY, vec.zCoord) > 0.9D) {
					ent.getMoveHelper().setMoveTo(vec.xCoord, vec.yCoord, vec.zCoord, speed);
				} else {
					//ent.rotationYaw = limitAngle((float)ent.rotationYaw, (float)ai.activeFormation.smoothYaw, 1F);//(float) ai.activeFormation.smoothYaw;
				}
			} else {
				//System.out.println("CRITICAL ERROR IN FORMATION!");
			}
		} else {
			ticksBeforeFormationRetry = 60;
			if (vec != null) {
				ent.getNavigator().clearPathEntity();
				if (ent.getNavigator().noPath() || ent.worldObj.getWorldTime() % 10 == 0) {
					PFQueue.getPath(ent, (int)vec.xCoord, (int)vec.yCoord, (int)vec.zCoord, ai.maxPFRange);
				}
				
			} else {
				//System.out.println("CRITICAL ERROR IN FORMATION PATH AROUND!");
			}
		}
	}
	
	public float limitAngle(float par1, float par2, float par3)
    {
        float f3 = MathHelper.wrapAngleTo180_float(par2 - par1);

        if (f3 > par3)
        {
            f3 = par3;
        }

        if (f3 < -par3)
        {
            f3 = -par3;
        }

        return par1 + f3;
    }
	
	public void onTickCloseCombat() {
		//ai.lungeFactor = 1.2F;

		if (entInt.isBreaking()) return;
		if (ent.isCollidedHorizontally && ent.onGround) ent.jump();
		
		//temp
		//ent.setMoveSpeed(0.35F);
		//ent.entityCollisionReduction = 0.1F;
		//ent.lungeFactor = 1.05F;
		
		float closeFactor = 1F;
		float leadFactor = 1F;
		float checkAheadFactor = 1F;
		
		if (ent.getDistanceToEntity(ai.entityToAttack) > 3F) {
			leadFactor = ai.moveLeadFactorDist;
			//closeFactor = 0.5F;
		}
		
		if (!ent.onGround) {
			closeFactor = 0.1F;
		}
		
    	//System.out.println("closest ent target");
		
		float speed = c_CoroAIUtil.getMoveSpeed(ent) * ai.lungeFactor * closeFactor;
		double leadTicks = ai.moveLeadTicks;
		double vecX = (ai.entityToAttack.posX - targPrevPosX) * (leadTicks * leadFactor);
		double vecZ = (ai.entityToAttack.posZ - targPrevPosZ) * (leadTicks * leadFactor);
		
		//double checkX = (ai.entityToAttack.posX - targPrevPosX) * (leadTicks * checkAheadFactor);
		//double checkZ = (ai.entityToAttack.posZ - targPrevPosZ) * (leadTicks * checkAheadFactor);
		/*vecX = ent.posX - (ai.entityToAttack.posX + ((ai.entityToAttack.posX - targPrevPosX) * leadTicks));
		vecY = ent.posY - (ai.entityToAttack.posY + ((targ.posY - targPrevPosY) * leadTicks));
		vecZ = ent.posZ - (ai.entityToAttack.posZ + ((ai.entityToAttack.posZ - targPrevPosZ) * leadTicks));*/
		//ent.getMoveHelper().setMoveTo(vecX, vecY, vecZ, speed);
		double dist = ent.getDistanceToEntity(ai.entityToAttack);
		if (dist <= 3D || isMovementSafe()) {
			
			//ent.getNavigator().clearPathEntity();
			if (dist > 1D) {
				ent.getMoveHelper().setMoveTo(ai.entityToAttack.posX + vecX, ai.entityToAttack.posY, ai.entityToAttack.posZ + vecZ, speed);
			} else {
				//nadda?
			}
		} else {
			//System.out.println("not safe!");
			//ent.motionY = 0.5D;
			ticksBeforeCloseCombatRetry = 60;
			ai.checkPathfindLock();
    		ent.getNavigator().onUpdateNavigation();
    		ai.tickMovementHelp();
			//ent.getMoveHelper().
		}
		//System.out.println("lunging!: " + ent.getMoveSpeed() + " - " + ent.lungeFactor + " - " + speed);
		
		//ent.getDataWatcher().updateObject(20, 1);
		
		//System.out.println("TEMP OFF FOR REFACTOR");
		
		//jump over drops
		/*MovingObjectPosition aim = ai.getAimBlock(-2, true);
    	if (aim != null) {
    		if (aim.typeOfHit == EnumMovingObjectType.TILE) {
    			
    		}
    	} else {
    		if (ent.onGround) {
    			c_CoroAIUtil.jump(ent);
    		}
    	}
		
    	if (ent.onGround && ent.isCollidedHorizontally && !ent.isBreaking()) {
    		c_CoroAIUtil.jump(ent);
		}*/
		
		targPrevPosX = ai.entityToAttack.posX;
		targPrevPosY = ai.entityToAttack.posY;
		targPrevPosZ = ai.entityToAttack.posZ;
	}
	
	public boolean isMovementSafe() {

		double distStart = 0.5D;
		double distEnd = 2D;
		double distStep = 0.5D;
		
		double lookStartStop = 90D;
		double lookStep = 90D;
		
		return isMovementSafe(true, true, true, distStart, distEnd, distStep, lookStartStop, lookStep);
	}
	
	public boolean isMovementSafe(boolean checkThreats, boolean checkDrops, boolean checkWalls, double distStart, double distEnd, double distStep, double lookStartStop, double lookStep) {
		boolean safe = true;
		
		Entity center = ent;
		double adjAngle;
		double dist;
		
		for (double lookAheadDist = distStart; lookAheadDist <= distEnd; lookAheadDist += distStep) {
			for (adjAngle = -lookStartStop - lookStep; adjAngle <= lookStartStop; adjAngle += lookStep) {
				dist = lookAheadDist;
				
				double posX = (center.posX - Math.sin((-center.rotationYaw + adjAngle) * 0.01745329D) * dist);
				double posY = (center.boundingBox.minY/* - 0.3D - Math.sin((center.rotationPitch) / 180.0F * 3.1415927F) * dist*/);
				double posZ = (center.posZ + Math.cos((-center.rotationYaw + adjAngle) * 0.01745329D) * dist);
				
				int xx = (int)posX;
				int yy = (int)(posY - 0.5D);
				int groundAheadY = yy;
				int legsAheadY = yy+1;
				int headAheadY = yy+2;
				yy = (int)(posY - 0.5D);
				int zz = (int)posZ;
				
				if (checkThreats) {
					int lookAheadIDDrop = ent.worldObj.getBlockId(xx, yy, zz);
					int lookAheadIDCollide = ent.worldObj.getBlockId(xx, legsAheadY, zz);
					if (ent.onGround && ((lookAheadIDDrop == 0 || Block.blocksList[lookAheadIDDrop].blockMaterial == Material.lava || /*Block.blocksList[lookAheadIDDrop].blockMaterial == Material.water || */Block.blocksList[lookAheadIDDrop].blockMaterial == Material.cactus) || 
							(lookAheadIDCollide != 0 && (Block.blocksList[lookAheadIDCollide].blockMaterial == Material.lava || /*Block.blocksList[lookAheadIDDrop0].blockMaterial == Material.water || */Block.blocksList[lookAheadIDCollide].blockMaterial == Material.cactus)))) {
						safe = false;
						//System.out.println("drop alert!");
						break;
					} else if (ent.onGround && (lookAheadIDCollide != 0 && (Block.blocksList[lookAheadIDCollide].blockMaterial == Material.lava || /*Block.blocksList[lookAheadIDCollide].blockMaterial == Material.water || */Block.blocksList[lookAheadIDCollide].blockMaterial == Material.cactus))) {
						safe = false;
						//System.out.println("front alert!");
						break;
					}
				}
				
				if (checkDrops) {
					int lookAheadIDDrop0 = ent.worldObj.getBlockId(xx, yy, zz);
					int lookAheadIDDrop1 = ent.worldObj.getBlockId(xx, yy-1, zz);
					if (lookAheadIDDrop0 == 0 && lookAheadIDDrop1 == 0) {
						safe = false;
						break;
					}
				}
				
				if (checkWalls/* && adjAngle == 0 && lookAheadDist == 0.5D*/) {
					int lookAheadIDCollideTooHigh = ent.worldObj.getBlockId(xx, headAheadY, zz);
					//System.out.println("id " + lookAheadIDCollideTooHigh + " - " + xx + ", " + headAheadY + ", " + zz);
					//System.out.println(center.rotationYaw);
					//System.out.println("X-: " + Math.sin((-center.rotationYaw + adjAngle) * 0.01745329D) * dist);
					//System.out.println("Z+: " + Math.cos((-center.rotationYaw + adjAngle) * 0.01745329D) * dist);
					
					//System.out.println(lookAheadIDCollideTooHigh);
					if (ent.onGround && (lookAheadIDCollideTooHigh != 0 && Block.blocksList[lookAheadIDCollideTooHigh].blockMaterial.isSolid())) {
						safe = false;
						//System.out.println("front wall alert!");
						break;
					}
				}
			}
		}
		
		double posX = (center.posX - Math.cos((-center.rotationYaw) * 0.01745329D) * 1D);
		double posY = (center.boundingBox.minY/* - 0.3D - Math.sin((center.rotationPitch) / 180.0F * 3.1415927F) * dist*/);
		double posZ = (center.posZ + Math.sin((-center.rotationYaw) * 0.01745329D) * 1D);
		
		//this might not work yet....
		//if (ent.getDistanceToEntity(ai.entityToAttack) > ai.entityToAttack.getDistance(posX, posY, posZ)) {
			//System.out.println("cancel lead");
			//vecX = 0D;
			//vecZ = 0D;
		//}
		
		//if (!safe) System.out.println("not safe!");
		
		return safe;
	}
	
	public boolean isFacingWater(double distStart, double distEnd, double distStep, double lookStartStop, double lookStep) {
		Entity center = ent;
		double adjAngle;
		double dist;
		
		for (double lookAheadDist = distStart; lookAheadDist <= distEnd; lookAheadDist += distStep) {
			for (adjAngle = -lookStartStop - lookStep; adjAngle <= lookStartStop; adjAngle += lookStep) {
				dist = lookAheadDist;
				
				double posX = (center.posX - Math.sin((-center.rotationYaw + adjAngle) * 0.01745329D) * dist);
				double posY = (center.boundingBox.minY/* - 0.3D - Math.sin((center.rotationPitch) / 180.0F * 3.1415927F) * dist*/);
				double posZ = (center.posZ + Math.cos((-center.rotationYaw + adjAngle) * 0.01745329D) * dist);
				
				int xx = (int)posX;
				int yy = (int)(posY - 0.5D);
				int groundAheadY = yy;
				int legsAheadY = yy+1;
				int headAheadY = yy+2;
				yy = (int)(posY - 0.5D);
				int zz = (int)posZ;
				
				int lookAheadIDDrop0 = ent.worldObj.getBlockId(xx, yy, zz);
				
				if (isWater(lookAheadIDDrop0)) return true;
				
				if (lookAheadIDDrop0 == 0) {
					int scanDownY = yy - 1;
					for (int tries = 0; tries < 8; tries++) {
						int tryID = ent.worldObj.getBlockId(xx, scanDownY--, zz);
						if (tryID != 0) {
							if (isWater(tryID)) {
								return true;
							} else {
								return false;
							}
						}
					}
				}
				
			}
		}
		return false;
	}
	
	public boolean isWater(int id) {
		return id != 0 && Block.blocksList[id].blockMaterial == Material.water;
	}
	
	// Job shared functions //
	
	public void setPathToEntity(PathEntity pathentity)
    {
		entInt.getAIAgent().setPathToEntityForce(pathentity);
    }
	
	public boolean isEnemy(Entity ent) {
		return tamable.isTame() ? tamable.isEnemy(ent) : entInt.isEnemy(ent);
	}
	
	public boolean canJoinFormations() {
		return ai.canJoinFormations;
	}
}
