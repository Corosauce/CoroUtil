package CoroAI.entity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import CoroAI.PFQueue;

public class JobBase {
	
	public JobManager jm = null;
	public c_EnhAI ent = null;
	
	public EnumJobState state;
	
	//Shared job vars
	public int hitAndRunDelay = 0;
	public int tradeTimeout = 0;
	public int walkingTimeout;
	
	public int fleeDelay = 0;
	
	public JobBase() {
		
	}
	
	public JobBase(JobManager jm) {
		this.jm = jm;
		this.ent = jm.ent;
		setJobState(EnumJobState.IDLE);
	}
	
	public void setJobState(EnumJobState ekos) {
		state = ekos;
		//System.out.println("jobState: " + occupationState);
	}
	
	public boolean interact(EntityPlayer ent) {
		return false;
	}
	
	public void tradeConfirmCallback() {
		
	}

	public void tick() {
		if (hitAndRunDelay > 0) hitAndRunDelay--;
		if (tradeTimeout > 0) tradeTimeout--;
	}
	
	public boolean shouldExecute() {
		return true;
	}
	
	public boolean shouldContinue() {
		return true;
	}
	
	public void onLowHealth() {
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
		
		if (ent.onGround && ent.isCollidedHorizontally && !ent.isBreaking()) {
    		ent.jump();
		}
	}
	
	public void onIdleTick() {
		
		//slaughter entitycreature ai update function and put idle wander invoking code here
        if(((ent.getNavigator().noPath()) && ent.rand.nextInt(5) == 0/* || ent.rand.nextInt(80) == 0*/))
        {
        	
        	//System.out.println("home dist: " + ent.getDistance(ent.homeX, ent.homeY, ent.homeZ));
        	if (ent.getDistance(ent.homeX, ent.homeY, ent.homeZ) < ent.maxDistanceFromHome) {
        		if (ent.rand.nextInt(5) == 0) {
        			int randsize = 8;
            		ent.walkTo(ent, ent.homeX+ent.rand.nextInt(randsize) - (randsize/2), ent.homeY+0, ent.homeZ+ent.rand.nextInt(randsize) - (randsize/2),ent.maxPFRange, 600);
        		} else {
        			ent.updateWanderPath();
        		}
        		
        		
        	} else {
        		int randsize = 8;
        		ent.walkTo(ent, ent.homeX+ent.rand.nextInt(randsize) - (randsize/2), ent.homeY+0, ent.homeZ+ent.rand.nextInt(randsize) - (randsize/2),ent.maxPFRange, 600);
        	}
        } else {
        	if (ent.getNavigator().noPath()) {
    			ent.lookForItems();
        	}
        }
		
		
	}
	
	public void onJobRemove() {
		//Job cleanup stuff - 
		this.ent.setCurrentSlot(0);
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
	
	public void koaTrade(EntityPlayer ep) {
		
	}
	
	public void hitHook(DamageSource ds, int damage) {
		
	}
	
	// Blank functions //
	
	// Job shared functions \\
	
	public boolean checkHunger() {
		if (/*health < getMaxHealth() / 4 * 3 && */ent.getFoodLevel() <= 16) {
			//System.out.println("OH: " + + fakePlayer.foodStats.getFoodLevel()); 
			if (ent.eat()) {
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
	
	public boolean avoid(boolean actOnTrue) {
		Entity clEnt = null;
		float closest = 9999F;
		
		if (ent.lastFleeEnt != null && ent.lastFleeEnt.isDead) { ent.lastFleeEnt = null; }
		if (fleeDelay > 0) fleeDelay--;
		
		float range = 15F;
		
    	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(range, range/2, range));
        for(int j = 0; j < list.size(); j++)
        {
            Entity entity1 = (Entity)list.get(j);
            if(!entity1.isDead && ent.isEnemy(entity1))
            {
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
        	if (clEnt != ent.lastFleeEnt || (ent.getNavigator().noPath())) {
        		ent.lastFleeEnt = clEnt;
        		if (actOnTrue && fleeDelay <= 0) fleeFrom(clEnt);
        	}
        } else if (/*(ent.getNavigator().noPath()) && */ent.lastFleeEnt != null) {
    		if (actOnTrue && fleeDelay <= 0) fleeFrom(ent.lastFleeEnt);
        }
        
        //no idle wander for now
		if (ent.lastFleeEnt != null) {
			if (ent.lastFleeEnt.isDead) { ent.lastFleeEnt = null; }
			if (this.jm.priJob == EnumJob.HUNTER) setJobState(EnumJobState.W1);
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
		
		float look = ent.rand.nextInt(8)-4;
        //int height = 10;
        double dist = ent.rand.nextInt(8)+8;
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
        
        double homeDist = ent.getDistance(ent.homeX, ent.homeY, ent.homeZ);
        
        if (false && ent.job.getJobClass() instanceof JobHunt && homeDist > ent.maxDistanceFromHome / 4 * 3) {
        	ent.walkTo(ent, ent.homeX, ent.homeY, ent.homeZ, (float) homeDist/*ent.maxPFRange*/, 600);
        } else {
        	if (offset < 100) {
        		ent.walkTo(ent, gatherX, gatherY, gatherZ, ent.maxPFRange, 600, -1);
        	} else {
        		//System.out.println("flee failed");
            	ent.walkTo(ent, ent.homeX, ent.homeY, ent.homeZ, (float) homeDist/*ent.maxPFRange*/, 600);
        	}
        }
	}
	
	public boolean findWater() {
		
		int scanSize = ent.maxPFRange;
		int scanSizeY = 60;
		
		int tryX;// = ((int)this.posX) + rand.nextInt(scanSize)-scanSize/2;
		int tryY = ((int)ent.posY) - 1;
		int tryZ;// = ((int)this.posZ) + rand.nextInt(scanSize)-scanSize/2;
		
		int i = tryY + ent.rand.nextInt(scanSizeY)-scanSizeY/2;
		
		//System.out.println(tryX + " " + i + " " + tryZ);
		for (int ii = 0; ii <= 5; ii++) {
			tryX = ((int)ent.posX) + ent.rand.nextInt(scanSize)-scanSize/2;
			i = tryY + ent.rand.nextInt(scanSizeY)-scanSizeY/2;
			tryZ = ((int)ent.posZ) + ent.rand.nextInt(scanSize)-scanSize/2;
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
				
				ent.setState(EnumActState.WALKING);
				walkingTimeout = 300;
				ent.targX = tryX;
				ent.targY = tryY;
				ent.targZ = tryZ;
				//System.out.println(tryX + " " + i + " " + tryZ);
				return true;
				
			} else {
				//System.out.println("no water");
			}
		}
		
		return false;
	}
	
	public boolean nextNodeWater() {
		if (ent.pathToEntity != null && ent.pathToEntity.points.length > 2) {
			int index = ent.pathToEntity.pathIndex+1;
			if (index >= ent.pathToEntity.points.length) index = ent.pathToEntity.points.length-1;
			int x = ent.pathToEntity.points[index].xCoord;
			int y = ent.pathToEntity.points[index].yCoord;
			int z = ent.pathToEntity.points[index].zCoord;
			int id = ent.worldObj.getBlockId(x, y-1, z);
			
			//dont wanna use a loop here, enough goddamn loops already! just adjust lookup beyond the safe drop size
			if (id == 0) {
				y = ent.pathToEntity.points[index].yCoord - 5;
				id = ent.worldObj.getBlockId(x, y, z);
			}
			
			if (Block.blocksList[id] != null && Block.blocksList[id].blockMaterial == Material.water) {
				return true;
			}
		}
		return false;
	}
	
	public boolean findLand() {
		
		int scanSize = 64;
		
		int tryX = ((int)ent.posX) + ent.rand.nextInt(scanSize)-scanSize/2;
		int tryY = ((int)ent.posY) - 1;
		int tryZ = ((int)ent.posZ) + ent.rand.nextInt(scanSize)-scanSize/2;
		
		//System.out.println(this.worldObj.getBlockId(tryX, tryY, tryZ));
		for (int i = tryY; i > tryY - 10; i--) {
			if (ent.worldObj.getBlockId(tryX, i, tryZ) != 0 && !((Block.blocksList[ent.worldObj.getBlockId(tryX, i, tryZ)] != null && Block.blocksList[ent.worldObj.getBlockId(tryX, i, tryZ)].blockMaterial == Material.water))) {
				//System.out.println("found water");
				
				PFQueue.getPath(ent, tryX, tryY, tryZ, scanSize/2+6);
				
				
				
				
				//if (!this.hasPath()) { System.out.println("no path"); }
				
				ent.setState(EnumActState.WALKING);
				walkingTimeout = 300;
				ent.targX = tryX;
				ent.targY = tryY;
				ent.targZ = tryZ;
				
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
	
	public void onCloseCombatTick() {
		
		//ent.lungeFactor = 1.2F;
		
		if (ent.isBreaking()) return;
		
		//temp
		//ent.setMoveSpeed(0.35F);
		//ent.entityCollisionReduction = 0.1F;
		//ent.lungeFactor = 1.05F;
		
		
		
		float closeFactor = 1F;
		
		if (ent.getDistanceToEntity(ent.entityToAttack) < 1.1F) {
			closeFactor = 0.5F;
		}
		
		if (!ent.onGround) {
			closeFactor = 0.1F;
		}
		
		float speed = ent.getMoveSpeed() * ent.lungeFactor * closeFactor;
		ent.getMoveHelper().setMoveTo(ent.entityToAttack.posX, ent.entityToAttack.posY, ent.entityToAttack.posZ, speed);
		//System.out.println("lunging!: " + ent.getMoveSpeed() + " - " + ent.lungeFactor + " - " + speed);
		
		ent.getDataWatcher().updateObject(20, 1);
		
		//jump over drops
		MovingObjectPosition aim = ent.getAimBlock(-2, true);
    	if (aim != null) {
    		if (aim.typeOfHit == EnumMovingObjectType.TILE) {
    			
    		}
    	} else {
    		if (ent.onGround) {
    			ent.jump();
    		}
    	}
		
    	if (ent.onGround && ent.isCollidedHorizontally && !ent.isBreaking()) {
    		ent.jump();
		}
	}
	
	// Job shared functions //
	
}
