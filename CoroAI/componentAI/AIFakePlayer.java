package CoroAI.componentAI;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import tropicraft.entities.projectiles.EntityTropicraftLeafballNew;

import cpw.mods.fml.common.registry.LanguageRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ModLoader;
import net.minecraft.world.World;
import CoroAI.Behaviors;
import CoroAI.c_CoroAIUtil;
import CoroAI.entity.EntityThrowableUsefull;
import CoroAI.entity.EntityTropicalFishHook;
import CoroAI.entity.ItemTropicalFishingRod;
import CoroAI.entity.ItemTropicraftLeafball;
import CoroAI.entity.c_EntityPlayerMPExt;

public class AIFakePlayer {

	//fake player
	//item use cooldowns
	
	//handle food ticks, or make a food handling interface, but then who'd have the fakeplayer? rename this interface and manager to AIPlayer?
	//- renamed, but make this class just handle inventory and food objects with the fake player
	
	//Design rules/experience
	//- fake player does NOT like initializing on constructor, remember you have to watch and wait in tick for ...... a player instance to borrow off of?
	
	
	//Main AI reference
	public AIAgent ai;
	
	//Fake Player
	public InventoryPlayer inventory;
    public c_EntityPlayerMPExt fakePlayer;
    public NBTTagCompound cachedNBT;
    public IInvOutsourced inventoryOutsourced;
    
    //Syncing to client stuff
    public ItemStack heldItemLastState;

    //Item
    public int slot_Melee = 0;
	public int slot_Ranged = 1;
	public int slot_Tool = 2;
	public int rangedInUseTicksMax = 15;
	public boolean shouldLookForPickups = false;
	public boolean grabXP = false;
    public boolean grabItems = false;
	public List wantedItems;
	
	//Internal for item usage ticking
	public int rangedInUseCountdown = 0;
	public int rangedInUseTicks = 0;
	public boolean isCharging;
	public boolean speedChargeHack = false;
	public int coolDownRangedOutSource = 5;
	
	public float itemSearchRange = 5F;
	
	public EntityTropicalFishHook fishEntity;
	public float castingStrength = 1F;
    
    public AIFakePlayer(AIAgent parAI) {
    	ai = parAI;
    	wantedItems = new ArrayList();
    }
    
    public void initJobs() {
		setOccupationItems();
    }
    
    public void setOccupationItems() {
		if (this.inventory.mainInventory[0] != null) {
			System.out.println("Possible error - job items being added to populated inventory!");
		}
		ai.jobMan.priJob.setJobItems();
	}
    
    public void postFullInit() {
    	ai.postFullInit();
    	ai.dbg(ai.ent.entityId + " - CALLED: postFullInit()");
    	inventory = fakePlayer.inventory;
    	sync();
    	if (cachedNBT != null) loadInPlayerData(cachedNBT);
    	
    	if (ai.ent instanceof IInvUser) {
    		((IInvUser)ai.ent).postInitFakePlayer();
    	} else {
    		initJobs();
    	}
    	
    	//System.out.println("registering test 'dynamic' entity name for AIFakePlayer");
    	
    }
    
    public void updateTick() {
    	
    	if (ai.waitingToMakeFakePlayer) {
    		if (watchFakePlayerAndSync()) {
    			ai.waitingToMakeFakePlayer = false;
    			postFullInit();
    		}
    	}
    	
    	//Prevent AI processing if entity is initializing or dying
    	if (fakePlayer == null || ai.ent.getHealth() <= 0) return;
    	
    	tickCooldowns();
    	tickTargetFixing();
    	tickItemPickupScan();
    	
    }
    
    public void tickCooldowns() {
    	if (fakePlayer.xpCooldown > 0) { fakePlayer.xpCooldown--; }
    }
    
    public void tickItemPickupScan() {
    	List var3 = ai.ent.worldObj.getEntitiesWithinAABBExcludingEntity(ai.ent, ai.ent.boundingBox.expand(2.0D, 1.0D, 2.0D));
    	
        if(var3 != null) {
            for(int var4 = 0; var4 < var3.size(); ++var4) {
                Entity var5 = (Entity)var3.get(var4);

                if(!var5.isDead) {
                	if ((grabXP && (var5 instanceof EntityXPOrb)) || (grabItems && (var5 instanceof EntityItem))) {
                		var5.onCollideWithPlayer(fakePlayer);
                	}
                }
            }
        }
    }
    
    public boolean watchFakePlayerAndSync() {
    	//if (!ai.ent.worldObj.isRemote) {
    		
    		boolean success = false;
    	
    		//if chunk doesnt exist on ai spawn, it cant make fakeplayer due to fake player constructor lookup up chunk and initializing endless ai load loop
    		if (fakePlayer == null) {
                fakePlayer = newFakePlayer(ai.ent.worldObj);
            }
    		
    		if (fakePlayer != null) { 
    			if(fakePlayer.playerNetServerHandler == null) {
    		
		    		try {
		            	//fakePlayer = newFakePlayer(worldObj);
		            	
		    			if (ai.ent.worldObj.playerEntities.size() > 0)
		    	        {
		    	            if (ai.ent.worldObj.playerEntities.get(0) instanceof EntityPlayerMP)
		    	            {
		    	                fakePlayer.playerNetServerHandler = ((EntityPlayerMP)ai.ent.worldObj.playerEntities.get(0)).playerNetServerHandler;
		    	                fakePlayer.dimension = ((EntityPlayerMP)ai.ent.worldObj.playerEntities.get(0)).dimension;
		    	                
		    	                success = true;
		    	                
		    	                /*if (fakePlayer != null) */((EntityPlayer)fakePlayer).username = "fakerPlayer_" + ai.ent.entityId;
				                
				                //if (fakePlayer != null) {
			                	
			            	    c_CoroAIUtil.playerToCompAILookup.put(fakePlayer.username, ai.entInt);
		    	            }
		    	        }
		    	        else
		    	        {
		    	            //System.out.println("fakeplayer has no netserverhandler, might behave oddly");
		    	        }
		            	
		            	//System.out.println("DEBUG FIX 3: ");
		                //if (fakePlayer != null) ((EntityPlayer)fakePlayer).username = "fakePlayer_";
		                
	            	    
	            	    
	                    
		                //}
		            } catch (Exception ex) {
		            	ex.printStackTrace();
		            	return false;
		            }
    			}
    		} else {
    			return false;
    		}
    	//}
    	
    	if (fakePlayer != null) {
	    	if (fakePlayer.isDead) { 
	    		c_CoroAIUtil.setHealth(ai.ent, 0);
	    	} else {
	    		if (fakePlayer.getHealth() > 0) {
	    			ai.ent.deathTime = 0;
	    			sync();
	    		}
	    	}
    	}
    	
    	return success;
    }
    
	public void attackMelee(Entity ent, float dist) {
		if (inventory == null) return;
		this.setCurrentSlot(slot_Melee);
		leftClickItem(ent);
	}

	public void attackRanged(Entity ent, float dist) {
		if (inventory == null) return;
    	fakePlayer.faceEntity(ent, 180, 180);
		this.setCurrentSlot(slot_Ranged);
		rightClickItem();
	}
	
	public void rangedUsageUpdate(Entity ent, float dist) {
		if (inventory == null) return;
		sync();
		
		ItemStack itemInUse = fakePlayer.getItemInUse();
		
		if (itemInUse != null)
        {
            ItemStack itemstack = this.inventory.getCurrentItem();

            if (itemstack == itemInUse)
            {
            	//ai.dbg(ai.ent.entityId + " - rangedUsageUpdate() calling onUsingItemTick with usage of " + rangedInUseCountdown);
            	itemInUse.getItem().onUsingItemTick(itemInUse, fakePlayer, rangedInUseCountdown);
                if (this.rangedInUseCountdown <= 25 && this.rangedInUseCountdown % 4 == 0)
                {
                	fakePlayer.updateItemUse(itemstack, 5);
                }

                //hack fix to access internal item in use for editing
                fakePlayer.clearItemInUse();
                fakePlayer.setItemInUse(itemstack, --rangedInUseCountdown);
                
                if (rangedInUseCountdown == 0 && !ai.ent.worldObj.isRemote)
                {
                	//the order of these 2 lines might need more consideration
                	
                	//syncPreInvUsed();
                	fakePlayer.onItemUseFinish();
            		syncPostInvUsed();
                	ai.rangedUsageCancelCharge();
                }
            }
            else
            {
            	//syncPreInvUsed();
            	fakePlayer.clearItemInUse();
            	ai.rangedUsageCancelCharge();
            	syncPostInvUsed();
            }
        }
		
		//TEMP SETTING
		//rangedInUseTicksMax = 15;
		//ai.meleeOverridesRangedInUse = true;
		//ai.rangedAimWhileInUse = false;
		
		rangedInUseTicks++;
		
		if (rangedInUseTicks > rangedInUseTicksMax) {
			//System.out.println("rangedUsageUpdate ticks maxed: " + rangedInUseTicks + " - " + rangedInUseTicksMax);
			rangedInUseTicks = 0;
			//syncPreInvUsed();
			
			
			
			if (speedChargeHack) {
				ItemStack itemstack = this.inventory.getCurrentItem();
				if (itemstack != null) {
					fakePlayer.clearItemInUse();
					fakePlayer.setItemInUse(itemstack, 1);
				}
			}
			ai.rangedUsageCancelCharge();
			syncPostInvUsed();
		}
	}
	
	public void leftClickItem(Entity var1) {
		syncPreInvUsed();
    	try {
    		fakePlayer.attackTargetEntityWithCurrentItem(var1);
    		if (var1 instanceof EntityLiving) {
    			if (((EntityLiving) var1).getAITarget() == fakePlayer) {
    				
    			}
    			
    		}
    		/*if (this.getCurrentEquippedItem() == null) {
    			attackEntityWithNothing(var1);
    		} else {
    			
    		}*/
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	ai.ent.swingItem();
    }
	
	public void tickTargetFixing() {
		
    	if (ai.entityToAttack == fakePlayer) ai.entityToAttack = null;
    	if (ai.entityToAttack instanceof EntityPlayer && ((EntityPlayer)ai.entityToAttack).username.contains("fakePlayer")) ai.entityToAttack = null;
    	if (ai.entityToAttack instanceof EntityPlayer && ((EntityPlayer)ai.entityToAttack).username.contains("fakerPlayer")) ai.entityToAttack = null;
    	try {
    		if (ai.entityToAttack instanceof ICoroAI && ai.dipl_info != null && !ai.dipl_info.isEnemy(((ICoroAI)ai.entityToAttack).getAIAgent().dipl_info)) ai.entityToAttack = null;
    	} catch (Exception ex) { }
    	
		int huntRange = 8;
		
		List list = ai.ent.worldObj.getEntitiesWithinAABB(EntityLiving.class, ai.ent.boundingBox.expand(huntRange, huntRange/2, huntRange));
        for(int j = 0; j < list.size(); j++)
        {
        	EntityLiving ent = (EntityLiving)list.get(j);
            
        	if (ent != null && !ent.isDead && ai.entInt.isEnemy(ent)) {
    			Behaviors.checkOrFixTargetTasks(ent, ICoroAI.class);
        		if (ent.getAITarget() == fakePlayer) {
        			//ai.dbg("fix area melee revenge targetting on:" + ent);
    				ent.setRevengeTarget(ai.ent);
    				//ent.setAttackTarget(ai.ent);
    				ent.setLastAttackingEntity(ai.ent);
        		}
        	}
        }
	}
	
	public void rangedUsageCancelCharge() {
		if (ai.entityToAttack != null) {
			fakePlayer.faceEntity(ai.entityToAttack, 180, 180);
			ItemStack itemToUse = inventory.mainInventory[inventory.currentItem];
			if (itemToUse != null && itemToUse.getItem() instanceof ItemBow) {
				double adj = ai.entityToAttack.getDistanceToEntity(fakePlayer) / 5D;
				fakePlayer.rotationPitch += 4;
				//System.out.println("turret pitch adjust: " + adj);
				fakePlayer.rotationPitch -= adj;
			}
		}
		fakePlayer.stopUsingItem();
		setPrjOwner(1);
		rangedInUseCountdown = 0;
		rangedInUseTicks = 0;
	}
	
	public void rightClickItem() {
    	
    	boolean wasFired = true;
		sync();
		syncPreInvUsed();
    	
		ItemStack itemToUse = inventory.mainInventory[inventory.currentItem];
		if (itemToUse != null) {
			Item ii = itemToUse.getItem();
			if (ii != null) {
				//tropicraft specific ones
				if (itemToUse.getItem() instanceof ItemTropicalFishingRod) {
					((ItemTropicalFishingRod)itemToUse.getItem()).onItemRightClick3(itemToUse, ai.ent.worldObj, this, castingStrength);
					setPrjOwner(1);
					syncPostInvUsed();
					//((ItemTropicalFishingRod)itemToUse.getItem()).onItemRightClick2(itemToUse, worldObj, this, castingStrength);
				} else {
					if (itemToUse.getItem() instanceof ItemTropicraftLeafball) {
						ai.ent.rotationPitch -= 2;
					}

					ItemStack itemstack = inventory.mainInventory[inventory.currentItem];
					Item item = itemstack.getItem();
					//wasFired = c_CoroAIUtil.AIRightClickHook(this, itemToUse);
					
					boolean forceHoldAndRelease = ItemUsageProfiles.shouldItemChargeFire(itemstack);
					
					try {
						//ai.dbg(ai.ent.entityId + " - using item right click!");
						/*if (!forceHoldAndRelease) */
						itemToUse.useItemRightClick(ai.ent.worldObj, fakePlayer);
						ai.ent.swingItem();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					if (forceHoldAndRelease || itemToUse.getItem() instanceof ItemBow || itemToUse.getItem() instanceof ItemFood) {
						//fakePlayer.itemInUseCount = 0;
						
						boolean oldMethod = false;
						
						if (!oldMethod && item != null) {
							isCharging = true;
							
							//set to 1, because onUpdate performs -- and checks if == 0
							rangedInUseCountdown = Item.itemsList[itemstack.itemID].getMaxItemUseDuration(itemstack);
							
							//fakePlayer.setItemInUse(itemstack, rangedInUseCountdown);
							ai.rangedUsageStartCharge();
							
							//ai.dbg(ai.ent.entityId + " - marked item in use!");
							
							/*int useTickMax = 5;
							
							for (int i = 0; i < useTickMax; i++) {
								item.onUsingItemTick(itemstack, fakePlayer, --inUseCount);
							}
							inUseCount = 1;
							c_CoroAIUtil.setPrivateValueSRGMCP(EntityPlayer.class, fakePlayer, "field_71072_f", "itemInUseCount", inUseCount);*/
							
							//fakePlayer.stopUsingItem();
							
							if (itemToUse.getItem() instanceof ItemFood) {
								ItemFood food = (ItemFood) itemToUse.getItem();
								
								//food.onFoodEaten(itemToUse, worldObj, fakePlayer);
								fakePlayer.getFoodStats().addStats(food);
							}
							
							
							
							isCharging = false;
						}
						
						if (oldMethod) {
							//not quited used yet, must code item switch lockout code before
							
							isCharging = true;
							
							//set to 1, because onUpdate performs -- and checks if == 0
							int inUseCount = Item.itemsList[itemstack.itemID].getMaxItemUseDuration(itemstack);
							
							fakePlayer.setItemInUse(itemstack, inUseCount);
							
							inUseCount = 1;
							
							if (itemToUse.getItem() instanceof ItemFood) {
								ItemFood food = (ItemFood) itemToUse.getItem();
								
								//food.onFoodEaten(itemToUse, worldObj, fakePlayer);
								fakePlayer.getFoodStats().addStats(food);
							}
							
							c_CoroAIUtil.setPrivateValueSRGMCP(EntityPlayer.class, fakePlayer, "field_71072_f", "itemInUseCount", inUseCount);
							
							isCharging = false;
							fakePlayer.stopUsingItem();
						}
					} else {
						setPrjOwner(1);
						syncPostInvUsed();
					}
					//useInvItem(itemToUse);
				}
				
				/*if (itemToUse.getItem() instanceof ZCSdkItemGun && !(itemToUse.getItem() instanceof ZCSdkItemGunFlamethrower)) {
					if (wasFired) {
						setPrjOwner(1); //bullet casings disabled, offset is now 1
					}
				} else {
					setPrjOwner(1);
				}*/
			}
		}
	}
	
	public void setPrjOwner(int offset) {
    	Entity ent = (Entity)ai.ent.worldObj.loadedEntityList.get(ai.ent.worldObj.loadedEntityList.size()-offset);
    	if (ent instanceof EntityThrowable) {
    		//((EntityThrowable) ent).posY += 1.5F;
    		//setThrower(((EntityThrowable) ent), this);
    		c_CoroAIUtil.setPrivateValueBoth(EntityThrowable.class, ((EntityThrowable) ent), c_CoroAIUtil.refl_thrower_obf, c_CoroAIUtil.refl_thrower_mcp, ai.ent);
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
		} else if (ent instanceof EntityArrow) {
			((EntityArrow) ent).posY += 0.5F;
			((EntityArrow) ent).shootingEntity = ai.ent;
			((EntityArrow) ent).canBePickedUp = 0;
		} else if (ent instanceof EntityTropicalFishHook) {
			((EntityTropicalFishHook) ent).angler = ai.ent;
		} else if (ent instanceof EntityThrowableUsefull) {
			((EntityThrowableUsefull) ent).thrower = ai.ent;
		} else {
			//c_CoroAIUtil.setPrjOwnerHook(this, ent);
		}
		
		//System.out.println(worldObj.loadedEntityList.get(worldObj.loadedEntityList.size()-1));
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
	
	public void setCurrentSlot(int slot) {
    	if (inventory == null) { return; }
    	inventory.currentItem = slot;
    	syncClientItems();
    	//sync();
    }
	
	public ItemStack getCurrentEquippedItem()
    {
    	if (inventory == null) { return null; }
        return inventory.mainInventory[inventory.currentItem];
    }
	
	public void sync() {
    	if (fakePlayer == null) return;
    	fakePlayer.posX = ai.ent.posX;
    	fakePlayer.posY = ai.ent.posY - 0.5F;
    	fakePlayer.posZ = ai.ent.posZ;
    	fakePlayer.prevPosX = ai.ent.prevPosX;
    	fakePlayer.prevPosY = ai.ent.prevPosY;
    	fakePlayer.prevPosZ = ai.ent.prevPosZ;
    	fakePlayer.rotationPitch = ai.ent.rotationPitch;
    	fakePlayer.rotationYaw = ai.ent.rotationYaw;
    	fakePlayer.prevRotationPitch = ai.ent.prevRotationPitch;
    	fakePlayer.prevRotationYaw = ai.ent.prevRotationYaw;
    	
    	try {
    		if (inventory != null) {
		    	ai.ent.setCurrentItemOrArmor(0, inventory.mainInventory[0]);
		    	ai.ent.setCurrentItemOrArmor(1, inventory.armorInventory[0]);
		    	ai.ent.setCurrentItemOrArmor(2, inventory.armorInventory[1]);
		    	ai.ent.setCurrentItemOrArmor(3, inventory.armorInventory[2]);
		    	ai.ent.setCurrentItemOrArmor(4, inventory.armorInventory[3]);
    		}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	
    	c_CoroAIUtil.setHealth(ai.ent, ai.ent.getHealth());
    }
	
	public void syncPreInvUsed() {
    	if (inventoryOutsourced != null) inventoryOutsourced.syncOutsourcedToEntInventory(this);
	}
	
	public void syncPostInvUsed() {
		if (inventoryOutsourced != null) inventoryOutsourced.syncEntToOutsourcedInventory(this);
	}
	
	public void syncClientItems() {
    	if (!ai.ent.worldObj.isRemote) {
	        ByteArrayOutputStream bos = new ByteArrayOutputStream(140);
			DataOutputStream dos = new DataOutputStream(bos);
			
			ItemStack is = getCurrentEquippedItem();
			
			try {
				dos.writeInt(ai.ent.entityId);
				Packet.writeItemStack(is, dos);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			Packet250CustomPayload pkt = new Packet250CustomPayload();
			pkt.channel = "CoroAI_Inv";
			pkt.data = bos.toByteArray();
			pkt.length = bos.size();
			//pkt.isChunkDataPacket = true;
			MinecraftServer.getServer().getConfigurationManager().sendToAllNear(ai.ent.posX, ai.ent.posY, ai.ent.posZ, 64, this.fakePlayer.dimension, pkt);
			//System.out.println("sending out item sync packet: " + is.itemID);
        }
    }
	
	public c_EntityPlayerMPExt newFakePlayer(World world)
    {
        MinecraftServer mc = MinecraftServer.getServer();

        //this.setDead();
        
        if (mc == null)
        {
            return null;
        }

        //int dim = world.worldInfo.getDimension();
        c_EntityPlayerMPExt player = null;//

        //System.out.println("DEBUG FIX 2: ");
        //player = new c_EntityPlayerMPExt(mc, world, "fakePlayer_", new ItemInWorldManager(world));
        if (c_CoroAIUtil.chunkExists(world, (int)(ai.ent.posX / 16), (int)(ai.ent.posZ / 16))) {
        	player = new c_EntityPlayerMPExt(mc, world, "fakerPlayer_" + ai.ent.entityId, new ItemInWorldManager(world));
        	//inventory = player.inventory;
        } else {
        	//System.out.println("Chunk doesnt exist, cant create fake player, posX: " + posX + " | posZ: " + posZ);
        }
        
        
        
        
        
        //System.out.println("c_CoroAIUtil.playerToAILookup.put(player.username, this); OFF");

        //mc.configManager.netManager
        //player.movementInput = new MovementInputFromOptions(mod_ZombieCraft.mc.gameSettings);
        return player;
    }
	
	public void readEntityFromNBT(NBTTagCompound var1) {
        //super.readEntityFromNBT(var1);
		//ai.dbg(ai.ent.entityId + " - CALLED: readEntityFromNBT()");
        try {
        	cachedNBT = var1;
        } catch (Exception ex) { ex.printStackTrace(); }
	}
    
    public void loadInPlayerData(NBTTagCompound var1) {
    	//ai.dbg(ai.ent.entityId + " - CALLED: loadInPlayerData()");
    	if (fakePlayer != null) {
	        NBTTagList var2 = var1.getTagList("Inventory");
	        this.inventory.readFromNBT(var2);
	        fakePlayer.dimension = var1.getInteger("Dimension");
	        //setSleeping(var1.getBoolean("Sleeping"));
	        //fakePlayer.sleepTimer = var1.getShort("SleepTimer");
	        fakePlayer.experience = var1.getFloat("XpP");
	        fakePlayer.experienceLevel = var1.getInteger("XpLevel");
	        fakePlayer.experienceTotal = var1.getInteger("XpTotal");
	
	        /*if(getSleeping()) {
	        	fakePlayer.playerLocation = new ChunkCoordinates(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
	        	fakePlayer.wakeUpPlayer(true, true, false);
	        }*/
	
	        /*if(var1.hasKey("SpawnX") && var1.hasKey("SpawnY") && var1.hasKey("SpawnZ")) {
	        	fakePlayer.playerSpawnCoordinate = new ChunkCoordinates(var1.getInteger("SpawnX"), var1.getInteger("SpawnY"), var1.getInteger("SpawnZ"));
	        }*/
	
	        fakePlayer.getFoodStats().readNBT(var1);
	        fakePlayer.capabilities.readCapabilitiesFromNBT(var1);
        }
    }

    public void writeEntityToNBT(NBTTagCompound var1) {
    	//ai.dbg(ai.ent.entityId + " - CALLED: writeEntityToNBT()");
        //super.writeEntityToNBT(var1);
        try {
        	if (fakePlayer != null) {
		        if (inventory != null) var1.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
		        var1.setInteger("Dimension", fakePlayer.dimension);
		        //var1.setBoolean("Sleeping", getSleeping());
		        //par1NBTTagCompound.setShort("SleepTimer", (short)this.sleepTimer);
		        var1.setFloat("XpP", fakePlayer.experience);
		        var1.setInteger("XpLevel", fakePlayer.experienceLevel);
		        var1.setInteger("XpTotal", fakePlayer.experienceTotal);
		
		        /*if(this.playerSpawnCoordinate != null) {
		            var1.setInteger("SpawnX", this.playerSpawnCoordinate.posX);
		            var1.setInteger("SpawnY", this.playerSpawnCoordinate.posY);
		            var1.setInteger("SpawnZ", this.playerSpawnCoordinate.posZ);
		        }*/
		
		        fakePlayer.getFoodStats().writeNBT(var1);
		        fakePlayer.capabilities.writeCapabilitiesToNBT(var1);
        	}
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    
    public boolean interactOld(EntityPlayer par1EntityPlayer) {
    	
    	//System.out.println(par1EntityPlayer.worldObj.isRemote);
    	//if did something 
    	//TEMP!!!!
    	if (!ai.ent.worldObj.isRemote && par1EntityPlayer.getCurrentEquippedItem() != null) {
    		ai.dbg("giving AIFakePlayer ranged item");
    		inventory.setInventorySlotContents(1, par1EntityPlayer.getCurrentEquippedItem().copy());
    		sync();
    		//ai.ent.setCurrentItemOrArmor(0, par1EntityPlayer.getCurrentEquippedItem().copy());
    		return true;
    	}
    	
    	return false;
    }
    
    public void cleanup() {
		if (fakePlayer != null) c_CoroAIUtil.playerToCompAILookup.remove(fakePlayer.username);
    	fakePlayer = null;
    	inventory = null;
    }
    
    public void onLivingUpdateTick() {
		if (ai.ent.worldObj.isRemote) {
			ItemStack is = ai.ent.getCurrentItemOrArmor(0);
			if (is != null) {
	    		is.getItem().onUpdate(is, ai.ent.worldObj, fakePlayer, 0, true);
	    	}
		} else {
			if (fakePlayer != null) {
				ItemStack is = fakePlayer.getCurrentEquippedItem();
				if (is != null) {
					is.getItem().onUpdate(is, ai.ent.worldObj, fakePlayer, fakePlayer.inventory.currentItem, true);
					if (heldItemLastState == null || !is.isItemEqual(heldItemLastState)) {
						heldItemLastState = is;
						syncClientItems();
					}
				}
			}
		}
	}
}
