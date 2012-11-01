package CoroAI.entity;
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import CoroAI.*;

public class c_PlayerProxy extends c_EntInterface implements c_IEnhPF {

	public MovingObjectPosition aimHit = null;
	public int blockID = Block.ladder.blockID;
	
	public boolean forcejump;
	public boolean mining;
	public int mineDelay;
	public int noMoveTicks;
	public int curCooldown_Melee;
	public int curCooldown_Ranged;
	public int pfTimeout;
	
    //EntityCreature overrides
    public PathEntityEx pathToEntity;
    public Entity entityToAttack;
    public boolean hasAttacked;
    //protected int fleeingTick;
    
    //Customizable item use vars
	public int cooldown_Melee;
	public int cooldown_Ranged;
	public int slot_Melee;
	public int slot_Ranged;
	public float maxReach_Melee;
	public float maxReach_Ranged;
	public int itemSearchRange;
	public List wantedItems;
	
	
	//Trading fields
	public int slot_Trade;
	public int tradeItemCount = 3;
	public int activeTradeItemSlot;
	public List uniqueTradeItems; 
	
	//Player Faking vars - moved to the EntInterface
	
	
	public int maxPFRange = 64;
	
	//something that should be moved?
	public EntityTropicalFishHook fishEntity;
	public float castingStrength = 1F;
	
	public boolean swingArm;
    public int swingTick;
	
    public boolean isCharging;
    
    public boolean grabXP = false;
    public boolean grabItems = false;
    
    public boolean serverMode = false;
    
    public c_PlayerProxy(World world) {
        super(world);
        swingArm = false;
        swingTick = 0;
        //texture = "/mob/zombie.png";
        //moveSpeed = 0.8F;
        //attackStrength = 5;
        //health = 40;
        
        if (fakePlayer != null) ((EntityPlayer)fakePlayer).username = "fakePlayer";

        if (fakePlayer != null) {
        	inventory = fakePlayer.inventory;
        	sync();
        } else { //client slave mode
        	inventory = new InventoryPlayer(null);
        }
        
        wantedItems = new LinkedList();
        slot_Melee = 0;
        slot_Ranged = 1;
        if (fakePlayer != null) slot_Trade = activeTradeItemSlot = inventory.mainInventory.length-1;
        cooldown_Melee = 10;
        cooldown_Ranged = 40;
        maxReach_Melee = 2.5F;
        maxReach_Ranged = 12F;
        itemSearchRange = 5;
        
        //custom mob code init
        //inventory.addItemStackToInventory(new ItemStack(Item.swordDiamond, 1));
        //inventory.addItemStackToInventory(new ItemStack(Item.bow, 1));
        //inventory.addItemStackToInventory(new ItemStack(Item.arrow, 32));
        //wantedItems.add(Item.arrow.shiftedIndex);
        
        
        //use canClimb() instead
        //pf.canClimb = true;
    }
    
    @Override
	protected void entityInit()
    {
		super.entityInit();
        //this.dataWatcher.addObject(22, Integer.valueOf(0)); //Current slot
    }
    
    protected EntityAnimal spawnBabyAnimal(EntityAnimal entityanimal)
    {
        return new EntityCow(worldObj);
    }
    
    public boolean interact(EntityPlayer var1) {
    	//ModLoader.getMinecraftInstance().displayGuiScreen(new GuiTrade(this.inventory, var1.inventory));
    	return false;
    }
    
    protected void attackEntity(Entity var1, float var2) {
    	sync();
    	aimAtEnt(var1);
    	if (isAimedAtTarget(var1)) {
	    	if (var2 < maxReach_Melee && var1.boundingBox.maxY > this.boundingBox.minY && var1.boundingBox.minY < this.boundingBox.maxY) {
	    		if (curCooldown_Melee <= 0) {
	    			this.setCurrentSlot(slot_Melee);
	        		leftClickItem(var1);
	        		this.curCooldown_Melee = cooldown_Melee;
	        	}
	    	} else if (var2 < maxReach_Ranged) {
	    		if (curCooldown_Ranged <= 0) {
	    			this.setCurrentSlot(slot_Ranged);
	        		rightClickItem();
	        		this.curCooldown_Ranged = cooldown_Ranged;
	    		}
	    	}
    	}
    }
    
    public void leftClickItem(Entity var1) {
    	try {
    		fakePlayer.attackTargetEntityWithCurrentItem(var1);
    	} catch (Exception ex) {
    		//ex.printStackTrace();
    	}
		swingItem();
    }
    
    public void rightClickItem() {
    	
    	boolean wasFired = true;
    	
		ItemStack itemToUse = inventory.mainInventory[inventory.currentItem];
		if (itemToUse != null) {
			Item ii = itemToUse.getItem();
			if (ii != null) {
				//tropicraft specific ones
				if (itemToUse.getItem() instanceof ItemTropicalFishingRod) {
					((ItemTropicalFishingRod)itemToUse.getItem()).onItemRightClick2(itemToUse, worldObj, this, castingStrength);
					//((ItemTropicalFishingRod)itemToUse.getItem()).onItemRightClick2(itemToUse, worldObj, this, castingStrength);
				} else {
					if (itemToUse.getItem() instanceof ItemTropicraftLeafball) {
						this.rotationPitch -= 2;
						sync();
					}
					
					wasFired = c_CoroAIUtil.AIRightClickHook(this, itemToUse);
					
					try {
						itemToUse.useItemRightClick(worldObj, fakePlayer);
					} catch (Exception ex) { 
						//ex.printStackTrace();
					}
					
					if (itemToUse.getItem() instanceof ItemBow || itemToUse.getItem() instanceof ItemFood) {
						//fakePlayer.itemInUseCount = 0;
						ItemStack itemstack = inventory.mainInventory[inventory.currentItem];
						//not quited used yet, must code item switch lockout code before
						isCharging = true;
						fakePlayer.setItemInUse(itemstack, Item.itemsList[itemstack.itemID].getMaxItemUseDuration(itemstack));
						
						//set to 1, because onUpdate performs -- and checks if == 0
						int inUseCount = 1;
						
						ItemFood food = (ItemFood) itemToUse.getItem();
						
						//food.onFoodEaten(itemToUse, worldObj, fakePlayer);
						fakePlayer.getFoodStats().addStats(food);
						
						c_CoroAIUtil.setPrivateValueBoth(EntityPlayer.class, fakePlayer, "f", "itemInUseCount", inUseCount);
						
						isCharging = false;
						//fakePlayer.stopUsingItem();
					}
					//useInvItem(itemToUse);
				}
				setPrjOwner(1);
				/*if (itemToUse.getItem() instanceof ZCSdkItemGun && !(itemToUse.getItem() instanceof ZCSdkItemGunFlamethrower)) {
					if (wasFired) {
						setPrjOwner(1); //bullet casings disabled, offset is now 1
					}
				} else {
					setPrjOwner(1);
				}*/
			}
		}
		swingItem();
	}
    
    public void switchItem(int id) {
    	if (Item.swordDiamond.shiftedIndex == id) {
    		curCooldown_Melee = 0;
    	} else if (Item.bow.shiftedIndex == id) {
    		
    	}
    	
    	this.setCurrentItem(id);
    }
    
    public boolean eat() {
		for(int j = 0; j < inventory.mainInventory.length; j++)
        {
            if(inventory.mainInventory[j] != null && isFood(inventory.mainInventory[j]))
            {
            	//inventory.consumeInventoryItem(j);
            	//setCurrentItem(mod_tropicraft.fishingRodTropical.shiftedIndex);
            	this.setCurrentSlot(j);
        		rightClickItem();
            	//health = fakePlayer.health;
            	return true;
            }
        }
		return false;
	}
	
	public boolean isFood(ItemStack itemstack) {
		if (itemstack != null) {
			Item item = itemstack.getItem();
			if (item instanceof ItemFood) {
				return true;
			}
		}
		return false;
	}
    
    public void dropCurrentItem()
    {
        dropPlayerItemWithRandomChoice(inventory.decrStackSize(inventory.currentItem, 1), false);
    }

    public void dropPlayerItem(ItemStack itemstack)
    {
        dropPlayerItemWithRandomChoice(itemstack, false);
    }

    public void dropPlayerItemWithRandomChoice(ItemStack itemstack, boolean flag)
    {
        if(itemstack == null)
        {
            return;
        }
        EntityItem entityitem = new EntityItem(worldObj, posX, (posY - 0.30000001192092896D) + (double)getEyeHeight(), posZ, itemstack);
        entityitem.delayBeforeCanPickup = 40;
        float f = 0.1F;
        if(flag)
        {
            float f2 = rand.nextFloat() * 0.5F;
            float f4 = rand.nextFloat() * 3.141593F * 2.0F;
            entityitem.motionX = -MathHelper.sin(f4) * f2;
            entityitem.motionZ = MathHelper.cos(f4) * f2;
            entityitem.motionY = 0.20000000298023224D;
        } else
        {
            float f1 = 0.3F;
            entityitem.motionX = -MathHelper.sin((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F) * f1;
            entityitem.motionZ = MathHelper.cos((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F) * f1;
            entityitem.motionY = -MathHelper.sin((rotationPitch / 180F) * 3.141593F) * f1 + 0.1F;
            f1 = 0.02F;
            float f3 = rand.nextFloat() * 3.141593F * 2.0F;
            f1 *= rand.nextFloat();
            entityitem.motionX += Math.cos(f3) * (double)f1;
            entityitem.motionY += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
            entityitem.motionZ += Math.sin(f3) * (double)f1;
        }
        worldObj.spawnEntityInWorld(entityitem);
    }
    
    public void aimAtEnt(Entity ent) {
    	this.faceEntity(ent, 30, 30);
    }
    
    public boolean isAimedAtTarget(Entity ent) {
    	//eventually put an 'is facing' angle check or something, prevent shooting before turning
    	return true;
    }
    
    public void faceCoord(int x, int y, int z, float f, float f1)
    {
        double d = x - posX;
        double d2 = z - posZ;
        double d1;
        d1 = y - (posY + (double)getEyeHeight());
        
        double d3 = MathHelper.sqrt_double(d * d + d2 * d2);
        float f2 = (float)((Math.atan2(d2, d) * 180D) / 3.1415927410125732D) - 90F;
        float f3 = (float)(-((Math.atan2(d1, d3) * 180D) / 3.1415927410125732D));
        rotationPitch = -updateRotation(rotationPitch, f3, f1);
        rotationYaw = updateRotation(rotationYaw, f2, f);
    }
    
    private float updateRotation(float f, float f1, float f2)
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
    
    public void swingItem()
    {
    	swingArm = true;
    	fakePlayer.addExhaustion(0.14F);
    }
    
    public boolean canClimb() {
    	return true;
    }
    
    protected Entity findPlayerToAttack()
    {
        EntityPlayer entityplayer = worldObj.getClosestPlayerToEntity(this, 16D);
        if(entityplayer != null && canEntityBeSeen(entityplayer))
        {
        	//NO!
            return null;
        } else
        {
            return null;
        }
    }
    
    public void getPathOrWalkableBlock(Entity entity, float f)
	{
    	PathEntity pathentity = null;//worldObj.getPathToEntity(this, entity, 16F, false, false, false, false);
		if(pathentity == null && f > 12F)
		{
			int i = MathHelper.floor_double(entity.posX) - 2;
			int j = MathHelper.floor_double(entity.posZ) - 2;
			int k = MathHelper.floor_double(entity.boundingBox.minY);
			for(int l = 0; l <= 4; l++)
			{
				for(int i1 = 0; i1 <= 4; i1++)
				{
					if((l < 1 || i1 < 1 || l > 3 || i1 > 3) && worldObj.isBlockNormalCube(i + l, k - 1, j + i1) && !worldObj.isBlockNormalCube(i + l, k, j + i1) && !worldObj.isBlockNormalCube(i + l, k + 1, j + i1))
					{
						setLocationAndAngles((float)(i + l) + 0.5F, k, (float)(j + i1) + 0.5F, rotationYaw, rotationPitch);
						return;
					}
				}

			}

		} else
		{
			setPathToEntity(pathentity);
		}
	}

    public void onLivingUpdate() {

        if(swingArm) {
            swingTick++;

            
            if(swingTick == 8) {
                swingTick = 0;
                swingArm = false;
            }
        } else {
            swingTick = 0;
        }

        swingProgress = (float)swingTick / 8F;
    	
    	if (this.worldObj.isRemote || fakePlayer == null) {
    		super.onLivingUpdate();
    		return;
    	}
    	
    	aimHit = rayTrace(1.0D, 1.0F);
    	
        if(!handleWaterMovement()/* && !mod_MinerZombie.isSameTeam(this)*/) {
            forcejump = false;
            mineDelay--;

            if(mineDelay < 1 && noMoveTicks > 10) {
                mineDelay = 3;
                mining = true;
                if (entityToAttack instanceof EntityLiving) {
                	ItemStack itemToUse = inventory.mainInventory[0];
                	//if (itemToUse.getItem() instanceof ItemBlock) {
                	//mod_MinerZombie.tryDig(this, (EntityLiving)entityToAttack);
	                	if (aimHit != null && aimHit.typeOfHit == EnumMovingObjectType.TILE) {
	                		if (worldObj.getBlockId(aimHit.blockX, aimHit.blockY, aimHit.blockZ) != 0 && worldObj.getBlockId(aimHit.blockX, aimHit.blockY, aimHit.blockZ) != Block.ladder.blockID) {
	                			//sync();
	                			//int var8 = itemToUse.getBlockId(var4, var5, var6);
	                			//if (!itemToUse.useItem(fakePlayer, worldObj, aimHit.blockX, aimHit.blockY, aimHit.blockZ, aimHit.sideHit)) {
	                				//useInvItem(itemToUse);
	                				
	                			//}
	                			//setPrjOwner();
	                			//System.out.println(worldObj.loadedEntityList.get(worldObj.loadedEntityList.size()-1));
	                			//itemToUse.useItem(itemToUse, this, this.worldObj, aimHit.blockX, aimHit.blockY, aimHit.blockZ, aimHit.sideHit);
	                		}
	                	}
                	/*} else {
                		useInvItem(itemToUse);
                		setPrjOwner();
                	}*/
                }
            }
//aimHit.entityHit
            //info = noMoveTicks;
        } else {
        	if(this.isCollidedHorizontally/* && !forcejump*/) {
                this.isJumping = true;
            } else {
            	this.isJumping = false;
            }
        }

        /*if(!mod_MinerZombie.hostilesBreakWallsB.get()) {
            setEntityDead();
        }*/
        
        //Cooldowns
        if (curCooldown_Melee > 0) { curCooldown_Melee--; }
        if (curCooldown_Ranged > 0) { curCooldown_Ranged--; }
        
        //Parts taken from EntityPlayer.onUpdate / onLivingUpdate
        
      //fakePlayer.health = 20;
        
        //fakePlayer.onUpdate();
        //health = fakePlayer.health;
        
        if (fakePlayer.getItemInUse() != null)
        {
            ItemStack itemstack = inventory.mainInventory[inventory.currentItem];
            if (itemstack != fakePlayer.getItemInUse())
            {
            	fakePlayer.clearItemInUse();
            }
            else
            {
            	//IS THIS NEEDED??? if items dont work, this could be it
            	//fakePlayer.getItemInUse().getItem().onUsingItemTick(fakePlayer.getItemInUse(), fakePlayer, fakePlayer.getItemInUseCount());
                if (fakePlayer.getItemInUseCount() <= 25 && fakePlayer.getItemInUseCount() % 4 == 0)
                {
                	updateItemUse(itemstack, 5);
                }
                c_CoroAIUtil.setPrivateValueBoth(EntityPlayer.class, fakePlayer, "f", "itemInUseCount", fakePlayer.getItemInUseCount()-1);
                if (fakePlayer.getItemInUseCount() == 0 && !worldObj.isRemote)
                {
                	onItemUseFinish();
                }
            }
        }
        if(fakePlayer.xpCooldown > 0) {
            --fakePlayer.xpCooldown;
        }
        fakePlayer.getFoodStats().onUpdate(fakePlayer);
        
        int healthDiff = getPlHealth() - health;
        if (healthDiff > 0) {
        	System.out.println("player proxy health: " + health + " | " + healthDiff);
        	fakePlayer.addExhaustion(0.18F * healthDiff);
        }
        
        health = getPlHealth();
        
        
        if (moveForward > 0) {
        	float werwrwer = moveForward;
        	werwrwer = moveForward;
        } else {
        	
        }
        
        super.onLivingUpdate();
        
        if(this.health > 0) {
            List var3 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(2.0D, 1.0D, 2.0D));

            if(var3 != null) {
                for(int var4 = 0; var4 < var3.size(); ++var4) {
                    Entity var5 = (Entity)var3.get(var4);

                    if(!var5.isDead) {
                    	if (var5 instanceof EntityCreature && isEnemy(var5) && this instanceof c_EnhAI && !(var5 instanceof c_EnhAI)) { Behaviors.enhanceMonsterAIClose((c_EnhAI)this, (EntityCreature)var5); }
                    	if ((grabXP || !(var5 instanceof EntityXPOrb)) && (grabItems || !(var5 instanceof EntityItem))) {
                    		var5.onCollideWithPlayer(fakePlayer);
                    	}
                        
                    }
                }
            }
        }
        
        
    }
    
    public boolean isEnemy(Entity entity1) {
		if(entity1 instanceof EntityLiving && !(entity1 instanceof EntityCreeper || entity1 instanceof EntityEnderman) && !(entity1 instanceof EntityPlayer) && !(entity1 == this) && (entity1 instanceof EntityAnimal || entity1 instanceof EntityMob) && !(entity1 instanceof c_EnhAI) ) {
			return true;
		}
		return false;
	}
    
    public void useInvItem(ItemStack itemstack) {
    	//sync();
    	
    }
    
    public float getEyeHeight()
    {
        return 1.62F;
    }

    protected void resetHeight()
    {
        yOffset = 1.62F;
    }
    
    public void setPrjOwner(int offset) {
    	Entity ent = (Entity)worldObj.loadedEntityList.get(worldObj.loadedEntityList.size()-offset);
    	if (ent instanceof EntityThrowable) {
    		((EntityThrowable) ent).posY += 1.5F;
    		setThrower(((EntityThrowable) ent), this);
			//((EntityThrowable) ent).doesArrowBelongToPlayer = false;
			/*try { 
				//FIX ME
				ModLoader.setPrivateValue(EntityThrowable.class, ent, "c", this);
			} catch (Exception ex) {
				try {
					ModLoader.setPrivateValue(EntityThrowable.class, ent, "throwingEntity", this);
				} catch (Exception ex2) {
					
				}
			}*/
		} else 
		if (ent instanceof EntitySnowball) {
			try { 
				//FIX ME
				c_CoroAIUtil.setPrivateValue(EntitySnowball.class, ent, "i have no idea", this);
			} catch (Exception ex) {
				try {
					c_CoroAIUtil.setPrivateValue(EntitySnowball.class, ent, "shootingEntity", this);
				} catch (Exception ex2) {
					
				}
			}
		} else if (ent instanceof EntityArrow) {
			((EntityArrow) ent).posY += 1F;
			((EntityArrow) ent).shootingEntity = this;
			//((EntityArrow) ent).doesArrowBelongToPlayer = false;
		} else if (ent instanceof EntityTropicalFishHook) {
			((EntityTropicalFishHook) ent).angler = this;
		} else {
			c_CoroAIUtil.setPrjOwnerHook(this, ent);
		}
		
		//System.out.println(worldObj.loadedEntityList.get(worldObj.loadedEntityList.size()-1));
    }
    
    public void sync() {
    	if (fakePlayer == null) return;
    	fakePlayer.posX = posX;
    	fakePlayer.posY = posY;
    	fakePlayer.posZ = posZ;
    	fakePlayer.prevPosX = prevPosX;
    	fakePlayer.prevPosY = prevPosY;
    	fakePlayer.prevPosZ = prevPosZ;
    	fakePlayer.rotationPitch = this.rotationPitch;
    	fakePlayer.rotationYaw = this.rotationYaw;
    	fakePlayer.prevRotationPitch = prevRotationPitch;
    	fakePlayer.prevRotationYaw = prevRotationYaw;
    	
    	setPlHealth(health);
    }
    
    public boolean attackEntityFrom(DamageSource par1DamageSource, int par2)
    {
    	boolean blah = super.attackEntityFrom(par1DamageSource, par2);
    	sync();
    	return blah;
    }
    
    public int getAimID(int yOffset) {
    	MovingObjectPosition aim = getAimBlock(yOffset);
    	if (aim != null && aim.typeOfHit == EnumMovingObjectType.TILE) {
    		return this.worldObj.getBlockId(aim.blockX, aim.blockY, aim.blockZ);
    	}
    	return 0;
    }
    
    public boolean isBlockWater(int id) {
    	return ((Block.blocksList[id] != null && Block.blocksList[id].blockMaterial == Material.water));
    }
    
    public boolean getAimIsWater(int yOffset) {
    	MovingObjectPosition aim = getAimBlock(yOffset);
    	if (aim != null && aim.typeOfHit == EnumMovingObjectType.TILE) {
    		if ((Block.blocksList[this.worldObj.getBlockId(aim.blockX, aim.blockY, aim.blockZ)] != null && Block.blocksList[this.worldObj.getBlockId(aim.blockX, aim.blockY, aim.blockZ)].blockMaterial == Material.water)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public MovingObjectPosition getAimBlock(int yOffset) {
    	return getAimBlock(yOffset, false);
    }
    
    public MovingObjectPosition getAimBlock(int yOffset, boolean noYaw) {
    	
    	if (true) return null;
    	
    	EntityLiving entityliving = this;
    	float f = 1.0F;
        float f1 = entityliving.prevRotationPitch + (entityliving.rotationPitch - entityliving.prevRotationPitch) * f;
    	float f3 = entityliving.prevRotationYaw + (entityliving.rotationYaw - entityliving.prevRotationYaw) * f;
    	if (noYaw) f3 = 0.00001F;
        //int i = (int)Math.floor((double)(f3 / 90F) + 0.5D);
        //f3 = (float)i * 90F;
        double d = entityliving.prevPosX + (entityliving.posX - entityliving.prevPosX) * (double)f;
        double d1 = ((entityliving.prevPosY + (entityliving.posY - entityliving.prevPosY) * (double)f + 1.6200000000000001D)) - (double)entityliving.yOffset + yOffset;
        double d2 = entityliving.prevPosZ + (entityliving.posZ - entityliving.prevPosZ) * (double)f;
        Vec3 vec3d = Vec3.createVectorHelper(d, d1, d2);
        float f4 = MathHelper.cos(-f3 * 0.01745329F - 3.141593F);
        float f5 = MathHelper.sin(-f3 * 0.01745329F - 3.141593F);
        float f6 = -MathHelper.cos(-f1 * 0.01745329F - 0.7853982F);
        float f7 = MathHelper.sin(-f1 * 0.01745329F - 0.7853982F);
        float f8 = f5 * f6;
        float f9 = f7;
        float f10 = f4 * f6;
        //entityliving.info = f3;
        double d3 = 2.0D;
        Vec3 vec3d1 = vec3d.addVector((double)f8 * d3, (double)f9 * d3, (double)f10 * d3);              // \/ water collide check
        MovingObjectPosition movingobjectposition = entityliving.worldObj.rayTraceBlocks_do(vec3d, vec3d1, true);

        int id = -1;
        
        if(movingobjectposition == null) {
            return null;
        }
        
        if(movingobjectposition.typeOfHit == EnumMovingObjectType.TILE) {
        	//id = worldObj.getBlockId(movingobjectposition.blockX, movingobjectposition.blockY, movingobjectposition.blockZ);
	    	//System.out.println(movingobjectposition.blockX + " - " + movingobjectposition.blockY + " - " + movingobjectposition.blockZ);
        }
        
        return movingobjectposition;
    }
    
    public void tryPlace(EntityCreature entityliving) {

        float f = 2.0F;
        float f1 = entityliving.prevRotationPitch + (entityliving.rotationPitch - entityliving.prevRotationPitch) * f;
        float f2 = entityliving.prevRotationPitch + (entityliving.rotationPitch - entityliving.prevRotationPitch) * f;
        f1 = 0.0F;
        float f3 = entityliving.prevRotationYaw + (entityliving.rotationYaw - entityliving.prevRotationYaw) * f;
        int i = (int)Math.floor((double)(f3 / 90F) + 0.5D);
        f3 = (float)i * 90F;
        double d = entityliving.prevPosX + (entityliving.posX - entityliving.prevPosX) * (double)f;
        double d1 = (entityliving.prevPosY + (entityliving.posY - entityliving.prevPosY) * (double)f + 1.6200000000000001D) - (double)entityliving.yOffset;
        double d2 = entityliving.prevPosZ + (entityliving.posZ - entityliving.prevPosZ) * (double)f;
        Vec3 vec3d = Vec3.createVectorHelper(d, d1, d2);
        float f4 = MathHelper.cos(-f3 * 0.01745329F - 3.141593F);
        float f5 = MathHelper.sin(-f3 * 0.01745329F - 3.141593F);
        float f6 = -MathHelper.cos(-f1 * 0.01745329F - 0.7853982F);
        float f7 = MathHelper.sin(-f1 * 0.01745329F - 0.7853982F);
        float f8 = f5 * f6;
        float f9 = f7;
        float f10 = f4 * f6;
        //entityliving.info = f3;
        double d3 = 1.0D;
        double d4 = 1.8D;
        double d5 = 0.050000000000000003D;
        Vec3 vec3d1 = vec3d.addVector((double)f8 * d3, (double)f9 * d3, (double)f10 * d3);
        MovingObjectPosition movingobjectposition = entityliving.worldObj.rayTraceBlocks_do(vec3d, vec3d1, true);

        if(movingobjectposition == null) {
            return;
        }

        boolean flag = false;
        
        /*int j = MathHelper.floor_float((float)(entityliving.posX - entityliving1.posX));
        int k = (int)(entityliving.posY - entityliving1.posY);
        int l = MathHelper.floor_float((float)(entityliving.posZ - entityliving1.posZ));
        

        if(j + l > 0) {
            if((float)(k / (j + l)) > 1.0F) {
                flag = true;
            }
        } else if(k > 2) {
            flag = true;
        }*/

        if(movingobjectposition.typeOfHit == EnumMovingObjectType.TILE || flag) {
            int i2 = f2 >= -20F ? 0 : 1;
            int i1;
            int j1;
            int k1;

            if(flag) {
                i1 = (int)entityliving.posX;
                j1 = (int)entityliving.posZ - 1;
                k1 = (int)entityliving.posZ;
            } else {
            	
            	
            	
                i1 = movingobjectposition.blockX;
                j1 = movingobjectposition.blockY + i2;
                k1 = movingobjectposition.blockZ;
            }

            int l1 = entityliving.worldObj.getBlockId(i1, j1, k1);

            if(l1 == 0 && (i2 == 1 || flag)) {
                j1--;
                l1 = entityliving.worldObj.getBlockId(i1, j1, k1);

                if(l1 == 0 && i2 == 1) {
                    j1--;
                    l1 = entityliving.worldObj.getBlockId(i1, j1, k1);
                }
            }

            /*if(i1 != entityliving.curBlockX || j1 != entityliving.curBlockZ) {
                entityliving.curBlockDmg = 0.0F;
                entityliving.curBlockX = i1;
                entityliving.curBlockZ = j1;
            }*/

            Block block = Block.blocksList[l1];
            //float f11 = MiningZombieDigPower.get() * (float)(entityliving.nearbyMinerCount + 1);

            if(block != null) {
                if(entityliving instanceof c_PlayerProxy) {
                    ((c_PlayerProxy)entityliving).swingArm = true;
                }

                
                //worldRef.setBlock(i1, j1, k1, Block.ladder.blockID);
				//Block.blocksList[Block.ladder.blockID].onBlockPlaced(worldRef, (int)player.posX+1, yy, (int)player.posZ, 5);
            }
        }
    }
    
    public boolean onItemUse(ItemStack var1, EntityLiving var2, World var3, int var4, int var5, int var6, int var7) {
        int var8 = var3.getBlockId(var4, var5, var6);
        if(var8 == Block.snow.blockID) {
           var7 = 0;
        } else if(var8 != Block.vine.blockID) {
           if(var7 == 0) {
              --var5;
           }

           if(var7 == 1) {
              ++var5;
           }

           if(var7 == 2) {
              --var6;
           }

           if(var7 == 3) {
              ++var6;
           }

           if(var7 == 4) {
              --var4;
           }

           if(var7 == 5) {
              ++var4;
           }
        }

        int id = var3.getBlockId(var4, var5, var6);
        
        if(var1.stackSize == 0) {
           return false;
        } else {
           var3.getClass();
           if(var5 == 128 - 1 && Block.blocksList[this.blockID].blockMaterial.isSolid()) {
              return false;
           } else if(/*id != 0 && */id != Block.ladder.blockID/* || var3.canBlockBePlacedAt(this.blockID, var4, var5, var6, false, var7)*/) {
              Block var9 = Block.blocksList[this.blockID];
              if(var3.setBlockAndMetadataWithNotify(var4, var5, var6, this.blockID, 5)) {
                 if(var3.getBlockId(var4, var5, var6) == this.blockID) {
                    //Block.blocksList[this.blockID].onBlockPlaced(var3, var4, var5, var6, var7);
                    Block.blocksList[this.blockID].onBlockPlacedBy(var3, var4, var5, var6, var2);
                 }

                 //var3.playSoundEffect((double)((float)var4 + 0.5F), (double)((float)var5 + 0.5F), (double)((float)var6 + 0.5F), var9.stepSound.stepSoundDir(), (var9.stepSound.getVolume() + 1.0F) / 2.0F, var9.stepSound.getPitch() * 0.8F);
                 --var1.stackSize;
              }

              return true;
           } else {
              return false;
           }
        }
     }

    protected String getLivingSound() {
        return "mob.zombie";
    }

    protected String getHurtSound() {
        return "mob.zombiehurt";
    }

    protected String getDeathSound() {
        return "mob.zombiedeath";
    }

    protected int getDropItemId() {
        return 0;
    }

    public ItemStack getHeldItem() {
    	if (getCurrentEquippedItem() != null) { return getCurrentEquippedItem(); } else { return null; /*return new ItemStack(Item.stick, 1);*/ }
        //return new ItemStack(Block.ladder, 1);
    }
    
    public ItemStack getCurrentEquippedItem()
    {
    	if (inventory == null) { return null; }
        return inventory.mainInventory[inventory.currentItem];
    }
    
    //Removing 0-9 slot lockings
    public void setCurrentItem(int id) {
    	int k = -1;
        k = getInventorySlotContainItem(id);
        
        if (k >= 0)
        {
        	inventory.currentItem = k;
        	//this.dataWatcher.updateObject(22, k);
            //return;
        }
        else
        {
            //return;
        }
        
        syncClientItems();
        
    	//inventory.setCurrentItem(slot, 0, false, false);
    	//sync();
    }
    
    public void syncClientItems() {
    	if (!worldObj.isRemote) {
	        ByteArrayOutputStream bos = new ByteArrayOutputStream(140);
			DataOutputStream dos = new DataOutputStream(bos);
			
			ItemStack is = this.getCurrentEquippedItem();
			
			try {
				dos.writeInt(this.entityId);
				Packet.writeItemStack(is, dos);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			Packet250CustomPayload pkt = new Packet250CustomPayload();
			pkt.channel = "CoroAI_Inv";
			pkt.data = bos.toByteArray();
			pkt.length = bos.size();
			//pkt.isChunkDataPacket = true;
			MinecraftServer.getServer().getConfigurationManager().sendToAllNear(posX, posY, posZ, 64, this.fakePlayer.dimension, pkt);
			//System.out.println("sending out item sync packet: " + is.itemID);
        }
    }
    
    public int getInventorySlotContainItem(int i)
    {
        for (int j = 0; j < inventory.mainInventory.length; j++)
        {
            if (inventory.mainInventory[j] != null && inventory.mainInventory[j].itemID == i)
            {
                return j;
            }
        }

        return -1;
    }
    
    public FoodStats getFoodStats() {
    	return fakePlayer.getFoodStats();
    }
	
	public int getFoodLevel() {
		return getFoodStats().getFoodLevel();
	}
    
    public void setCurrentSlot(int slot) {
    	if (inventory == null) { return; }
    	inventory.currentItem = slot;
    	syncClientItems();
    	//sync();
    }

    /*public boolean getCanSpawnHere() {
        if(rand.nextInt(15) == 0) {
            int i = MathHelper.floor_double(posX);
            int j = MathHelper.floor_double(boundingBox.minY);
            int k = MathHelper.floor_double(posZ);

            if(worldObj.getSavedLightValue(EnumSkyBlock.Sky, i, j, k) > rand.nextInt(32)) {
                return false;
            } else {
                int l = worldObj.getBlockLightValue(i, j, k);
                return l <= rand.nextInt(8) && super.getCanSpawnHere();
            }
        } else {
            return false;
        }
    }*/
    
    protected void walkTo(Entity var1, int x, int y, int z, float var2, int timeout) {
		System.out.println("this should never happen");
	}
    
    @Override
    public void updateWanderPath()
    {
        boolean flag = false;
        int i = -1;
        int j = -1;
        int k = -1;
        float f = -99999F;
        for (int l = 0; l < 10; l++)
        {
            int i1 = MathHelper.floor_double((posX + (double)rand.nextInt(13)) - 6D);
            int j1 = MathHelper.floor_double((posY + (double)rand.nextInt(7)) - 3D);
            int k1 = MathHelper.floor_double((posZ + (double)rand.nextInt(13)) - 6D);
            float f1 = getBlockPathWeight(i1, j1, k1);
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
        	walkTo(this, i, j, k, this.maxPFRange, 600);
        }
    }
    
    @Override
    protected void updateEntityActionState()
	{
    	
    	//dont want koa running..... for now?
    	fleeingTick = 0;
    	if(fleeingTick > 0)
        {
    		fleeingTick--;
        }
        hasAttacked = isMovementCeased();
        float f = 16F;
        if(entityToAttack == null)
        {
            entityToAttack = findPlayerToAttack();
            if(entityToAttack != null)
            {
            	PFQueue.getPath(this, entityToAttack, f);
            	//tryPath(entityToAttack, f);
                //pathToEntity = worldObj.getPathToEntity(this, entityToAttack, f);
            }
        } else
        if(!entityToAttack.isEntityAlive())
        {
            entityToAttack = null;
        } else
        {
            float f1 = entityToAttack.getDistanceToEntity(this);
            if(canEntityBeSeen(entityToAttack))
            {
                attackEntity(entityToAttack, f1);
            } else
            {
                //attackBlockedEntity(entityToAttack, f1);
            }
        }
        if(!hasAttacked && entityToAttack != null && (pathToEntity == null/* || rand.nextInt(20) == 0*/))
        {
        	PFQueue.getPath(this, entityToAttack, f);
        	//tryPath(entityToAttack, f);
            //pathToEntity = worldObj.getPathToEntity(this, entityToAttack, f);
        }
        int i = MathHelper.floor_double(boundingBox.minY + 0.5D);
        boolean flag = isInWater();
        boolean flag1 = handleLavaMovement();
        rotationPitch = 0.0F;
        if(pathToEntity == null/* || rand.nextInt(100) == 0*/)
        {
        	//Swticheroo
        	//super.updateEntityActionState();
        	livingAIUpdate();
            pathToEntity = null;
            return;
        }
        Vec3 vec3d = pathToEntity.getPosition(this);
        for(double d = width * 1.2F; vec3d != null && vec3d.squareDistanceTo(posX, vec3d.yCoord, posZ) < d * d;)
        {
            pathToEntity.incrementPathIndex();
            if(pathToEntity.isFinished())
            {
                vec3d = null;
                pathToEntity = null;
            } else
            {
                vec3d = pathToEntity.getPosition(this);
            }
        }

        isJumping = false;
        if(vec3d != null)
        {
            double d1 = vec3d.xCoord - posX;
            double d2 = vec3d.zCoord - posZ;
            double d3 = vec3d.yCoord - (double)i;
            float f2 = (float)((Math.atan2(d2, d1) * 180D) / 3.1415927410125732D) - 90F;
            float f3 = f2 - rotationYaw;
            moveForward = moveSpeed;
            for(; f3 < -180F; f3 += 360F) { }
            for(; f3 >= 180F; f3 -= 360F) { }
            if(f3 > 30F)
            {
                f3 = 30F;
            }
            if(f3 < -30F)
            {
                f3 = -30F;
            }
            rotationYaw += f3;
            if(hasAttacked && entityToAttack != null)
            {
                double d4 = entityToAttack.posX - posX;
                double d5 = entityToAttack.posZ - posZ;
                float f5 = rotationYaw;
                rotationYaw = (float)((Math.atan2(d5, d4) * 180D) / 3.1415927410125732D) - 90F;
                float f4 = (((f5 - rotationYaw) + 90F) * 3.141593F) / 180F;
                moveStrafing = -MathHelper.sin(f4) * moveForward * 1.0F;
                moveForward = MathHelper.cos(f4) * moveForward * 1.0F;
            }
            if(d3 > 0.0D)
            {
                isJumping = true;
            }
            if(shouldFaceTarget())
            {
                faceEntity(entityToAttack, 60F, 60F);
            }
        } else {
        	if(this.shouldFaceTarget()) {
                this.moveForward = this.moveSpeed;
                this.faceEntity(this.entityToAttack, 60F, 60F);
            } else {
            	//super.updateEntityActionState();
            }
        }
        
        if(isCollidedHorizontally && hasPath())
        {
            isJumping = true;
        }
        if(rand.nextFloat() < 0.8F && (flag || flag1))
        {
            isJumping = true;
        }
        
	}
    
    public boolean shouldFaceTarget() {
        //if (mod_PathingActivated.hasPetMod) {
        //System.out.println(state);
        //(new StringBuilder()).append("state - ").append(state).toString();

    	if(this.isCollidedHorizontally/* && !this.swingArm*/) {
            this.isJumping = true;
        } else {
        	this.isJumping = false;
        }
    	
        if(getEntityToAttack() != null) {

            if (getEntityToAttack() != null && (isSolidPath(getEntityToAttack()) || this.getPath() == null)) {
                this.moveForward = this.moveSpeed;
                faceEntity(getEntityToAttack(), 30.0F, 30.0F);
                //setPathExToEntity(null);
                return true;
            }

            return false;
            
        }

        //} else {

        

        /*if (((EntityCreature)this).getTarget() != null && (isSolidPath(((EntityCreature)this).getTarget()) || this.getPath() == null)) {
        	return true;
        }*/
        //}
        return false;
    }
    
    public boolean isSolidPath(Entity var1) {
        /*if (this.getDistanceToEntity(var1) > maxPFRange) {
            return true;
        }*/

        return this.canEntityBeSeen(var1) && (this.getDistanceToEntity(var1) < 4.0F) && Math.abs(this.posY - (double)this.yOffset - (var1.posY - (double)var1.yOffset)) <= 2.5D;
    }
    
    @Override
    public void onEntityUpdate() {
    	//??!?!!?!? REMOVE ME?!
    	//Behaviors.AI(this);
    	
    	//setDead();
    	
    	if (!worldObj.isRemote) {
    		if (fakePlayer == null && fakePlayer.playerNetServerHandler == null) {
	    		try {
	            	fakePlayer = newFakePlayer(worldObj);
	            	
	            	if (fakePlayer != null) ((EntityPlayer)fakePlayer).username = "fakePlayer";

	                if (fakePlayer != null) {
	                	inventory = fakePlayer.inventory;
	                	sync();
	                }
	            } catch (Exception ex) {
	            	ex.printStackTrace();
	            	return;
	            }
    		}
    	}
    	
    	if (fakePlayer != null) {
	    	if (fakePlayer.isDead) { 
	    		health = 0;
	    	} else {
	    		if (getPlHealth() > 0) {
	    			deathTime = 0;
	    			sync();
	    		}
	    	}
    	}
    	super.onEntityUpdate();
    }
    
    public int getHealth() {
    	return this.health;
    }
    
    public void setHealth(int val) {
    	this.health = val;
    	if (fakePlayer != null) this.setPlHealth(val);
    }
        
    public int getExp() {
    	return this.experienceValue;
    }
    
    public void setExp(int val) {
    	this.experienceValue = val;
    }
    
    @Override 
    public void setDead() {
    	if (fakePlayer != null) fakePlayer.setDead();
    	super.setDead();
    }
    
    @Override
    public void onDeath(DamageSource var1) {
        super.onDeath(var1);
        //use job items array instead for removal?
        if (!worldObj.isRemote && fakePlayer != null && this.inventory != null) {
	        this.inventory.mainInventory[0] = null;
	        this.inventory.mainInventory[1] = null;
	        
	        //remove the 2 items not last offered for trade
	        for (int i = 0; i < this.tradeItemCount; i++) {
	        	if (slot_Trade-i != activeTradeItemSlot) {
	        		//this.inventory.mainInventory[slot_Trade-i] = null;
	        	}
	        	
	        }
	        
	        this.inventory.dropAllItems();
        }
    }
    
    Entity currentTarget;
    public void livingAIUpdate() {
    	entityAge++;
        EntityPlayer entityplayer = worldObj.getClosestPlayerToEntity(this, -1D);
        despawnEntity();
        moveStrafing = 0.0F;
        moveForward = 0.0F;
        
        boolean flag = isInWater();
        boolean flag1 = handleLavaMovement();
        if(flag || flag1)
        {
            isJumping = rand.nextFloat() < 0.8F;
        }
        
        //Look code
        //if (true) return;
        float f = 8F;
        if(rand.nextFloat() < 0.02F)
        {
            //EntityPlayer entityplayer1 = worldObj.getClosestPlayerToEntity(this, f);
        	if (this.fishEntity != null) {
        		currentTarget = this.fishEntity;
        		c_CoroAIUtil.setPrivateValueBoth(EntityLiving.class, this, "bF", "currentTarget", currentTarget);
        	}
            /*if(entityplayer1 != null)
            {
            	
                currentTarget = entityplayer1;
                numTicksToChaseTarget = 10 + rand.nextInt(20);
            } else
            {
                randomYawVelocity = (rand.nextFloat() - 0.5F) * 20F;
            }*/
        }
        if(currentTarget != null)
        {
            faceEntity(currentTarget, 10F, getVerticalFaceSpeed());
            if(numTicksToChaseTarget-- <= 0 || currentTarget.isDead)
            {
                currentTarget = null;
            }
        } else
        {
            if(rand.nextFloat() < 0.05F)
            {
                randomYawVelocity = (rand.nextFloat() - 0.5F) * 20F;
            }
            rotationYaw += randomYawVelocity;
            rotationPitch = defaultPitch;
        }
    }
    
    public boolean getCanSpawnHere()
    {
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(boundingBox.minY);
        int k = MathHelper.floor_double(posZ);
        return super.getCanSpawnHere()/* && getBlockPathWeight(i, j, k) >= 0.0F*/;
    }

    public boolean hasPath()
    {
        return pathToEntity != null;
    }
    
    //Addition
    public PathEntityEx getPath() {
        return this.pathToEntity;
    }

    public void setPathToEntity(PathEntityEx pathentity)
    {
        pathToEntity = pathentity;
    }
    
    public void setPathExToEntity(PathEntityEx pathentity)
    {
        pathToEntity = pathentity;
    }
    
	/*public boolean tryPath(Entity var1, float var2) {
		if(var1 != null) {
			return tryPath((int)(var1.posX+0.5F), (int)(var1.boundingBox.minY), (int)(var1.posZ+0.5F), var2, false);
		} else {
			return false;
		}
    }
	
	public boolean tryPath(int x, int y, int z, float var2) {
        return tryPath(x, y, z, var2, false);
    }

    public boolean tryPath(int x, int y, int z, float var2, boolean override) {
        if (pfTimeout > 0 && !override) {
            return false;
        }
        //System.out.println("repathing, if this message spams, very bad");
    	//Get distance
    	float var3 = (float)this.getDistance(x, y, z);
        //float var3 = this.getDistanceToEntity(var1);

        //If too far, return 1 pathpoint with coords of endpoint
        if(var3 > var2) {
        	setPathToEntity(getEndPoint(x, y, z));
        	pfTimeout = 10;
            return false;
        } else {
        	pfTimeout = (int)var2*2 + rand.nextInt(50);
            setPathToEntity(worldObj.getEntityPathToXYZ(this, x, y, z, var2));
            if(this.getPath() == null) {
            	pfTimeout = (int)var2*2 + rand.nextInt(200);
            }

            return true;
        }
    }*/
    
    public PathEntity getEndPoint(Entity var1) {
    	PathPoint points[] = new PathPoint[1];
        points[0] = new PathPoint((int)(var1.posX-0.5), (int)(var1.posY + 0D), (int)(var1.posZ-0.5));
        return new PathEntity(points);
    }
    
    public PathEntity getEndPoint(int x, int y, int z) {
    	PathPoint points[] = new PathPoint[1];
        points[0] = new PathPoint(x, y, z);
        return new PathEntity(points);
    }

    public Entity getEntityToAttack()
    {
        return entityToAttack;
    }

    public void setEntityToAttack(Entity entity)
    {
        entityToAttack = entity;
    }

    public float getSpeedModifier()
    {
        float f = super.getSpeedModifier();
        if(fleeingTick > 0)
        {
            f *= 2.0F;
        }
        return f;
    }
    
    public boolean canCoordBeSeen(int x, int y, int z)
    {
        return worldObj.rayTraceBlocks(Vec3.createVectorHelper(posX, posY + (double)getEyeHeight(), posZ), Vec3.createVectorHelper(x, y, z)) == null;
    }
    
    public boolean canCoordBeSeenFromFeet(int x, int y, int z)
    {
        return worldObj.rayTraceBlocks(Vec3.createVectorHelper(posX, this.boundingBox.minY+0.15, posZ), Vec3.createVectorHelper(x, y, z)) == null;
    }
    
    //For mc 1.0
    public int getMaxHealth() {
    	return 20;
    }
    
    public void readEntityFromNBT(NBTTagCompound var1) {
        super.readEntityFromNBT(var1);
        try {
	        if (fakePlayer != null) {
		        NBTTagList var2 = var1.getTagList("Inventory");
		        this.inventory.readFromNBT(var2);
		        fakePlayer.dimension = var1.getInteger("Dimension");
		        setSleeping(var1.getBoolean("Sleeping"));
		        //fakePlayer.sleepTimer = var1.getShort("SleepTimer");
		        fakePlayer.experience = var1.getFloat("XpP");
		        fakePlayer.experienceLevel = var1.getInteger("XpLevel");
		        fakePlayer.experienceTotal = var1.getInteger("XpTotal");
		
		        if(getSleeping()) {
		        	fakePlayer.playerLocation = new ChunkCoordinates(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
		        	fakePlayer.wakeUpPlayer(true, true, false);
		        }
		
		        /*if(var1.hasKey("SpawnX") && var1.hasKey("SpawnY") && var1.hasKey("SpawnZ")) {
		        	fakePlayer.playerSpawnCoordinate = new ChunkCoordinates(var1.getInteger("SpawnX"), var1.getInteger("SpawnY"), var1.getInteger("SpawnZ"));
		        }*/
		
		        getFoodStats().readNBT(var1);
		        fakePlayer.capabilities.readCapabilitiesFromNBT(var1);
	        }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void writeEntityToNBT(NBTTagCompound var1) {
        super.writeEntityToNBT(var1);
        try {
        	if (fakePlayer != null) {
		        var1.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
		        var1.setInteger("Dimension", fakePlayer.dimension);
		        var1.setBoolean("Sleeping", getSleeping());
		        //par1NBTTagCompound.setShort("SleepTimer", (short)this.sleepTimer);
		        var1.setFloat("XpP", fakePlayer.experience);
		        var1.setInteger("XpLevel", fakePlayer.experienceLevel);
		        var1.setInteger("XpTotal", fakePlayer.experienceTotal);
		
		        /*if(this.playerSpawnCoordinate != null) {
		            var1.setInteger("SpawnX", this.playerSpawnCoordinate.posX);
		            var1.setInteger("SpawnY", this.playerSpawnCoordinate.posY);
		            var1.setInteger("SpawnZ", this.playerSpawnCoordinate.posZ);
		        }*/
		
		        getFoodStats().writeNBT(var1);
		        fakePlayer.capabilities.writeCapabilitiesToNBT(var1);
        	}
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void noMoveTriggerCallback() {
    	setPathExToEntity(null);
    	setEntityToAttack(null);
    }
    
    public float updateRotation2(float f, float f1, float f2)
    {
        float f3;
        for (f3 = f1 - f; f3 < -180F; f3 += 360F) { }
        for (; f3 >= 180F; f3 -= 360F) { }
        if (f3 > f2)
        {
            f3 = f2;
        }
        if (f3 < -f2)
        {
            f3 = -f2;
        }
        return f + f3;
    }
    
    public MovingObjectPosition rayTrace(double par1, float par3)
    {
        Vec3 var4 = this.getPosition(par3);
        Vec3 var5 = this.getLook(par3);
        Vec3 var6 = var4.addVector(var5.xCoord * par1, var5.yCoord * par1, var5.zCoord * par1);
        return this.worldObj.rayTraceBlocks(var4, var6);
    }
    
    public Vec3 getPosition(float par1)
    {
        if (par1 == 1.0F)
        {
            return Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
        }
        else
        {
            double var2 = this.prevPosX + (this.posX - this.prevPosX) * (double)par1;
            double var4 = this.prevPosY + (this.posY - this.prevPosY) * (double)par1;
            double var6 = this.prevPosZ + (this.posZ - this.prevPosZ) * (double)par1;
            return Vec3.createVectorHelper(var2, var4, var6);
        }
    }
}
