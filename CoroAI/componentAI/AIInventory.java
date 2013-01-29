package CoroAI.componentAI;

import net.minecraft.entity.Entity;
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
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.c_CoroAIUtil;
import net.minecraft.world.World;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import CoroAI.entity.EntityTropicalFishHook;
import CoroAI.entity.ItemTropicalFishingRod;
import CoroAI.entity.ItemTropicraftLeafball;
import CoroAI.entity.c_EntityPlayerMPExt;

public class AIInventory {

	//fake player
	//item use cooldowns
	
	//handle food ticks, or make a food handling interface, but then who'd have the fakeplayer? rename this interface and manager to AIPlayer?
	
	//Main AI reference
	public AIAgent ai;
	
	//Fake Player
	public InventoryPlayer inventory;
    public c_EntityPlayerMPExt fakePlayer;

    //Item
    public int slot_Melee = 0;
	public int slot_Ranged = 1;
	public boolean isCharging;
	public List wantedItems;
	
	public float itemSearchRange = 5F;
	
	public EntityTropicalFishHook fishEntity;
	public float castingStrength = 1F;
    
    public AIInventory(AIAgent parAI) {
    	ai = parAI;
    	wantedItems = new ArrayList();
    }
    
    public void initJobs() {
    	if (fakePlayer == null) {
            fakePlayer = newFakePlayer(ai.ent.worldObj);
        }
		setOccupationItems();
		
    }
    
    public void setOccupationItems() {
		if (this.inventory.mainInventory[0] != null) {
			System.out.println("Possible error - job items being added to populated inventory!");
		}
		ai.jobMan.priJob.setJobItems();
	}
    
    public void updateTick() {
    	
    	watchFakePlayerAndSync();
    	
    	if (fakePlayer == null) return;
    	
    	if (fakePlayer.xpCooldown > 0) { fakePlayer.xpCooldown--; }
    	
    	if (ai.entityToAttack == fakePlayer) ai.entityToAttack = null;
    	
    }
    
    public void watchFakePlayerAndSync() {
    	//if (!ai.ent.worldObj.isRemote) {
    		
    		//if chunk doesnt exist on ai spawn, it cant make fakeplayer due to fake player constructor lookup up chunk and initializing endless ai load loop
    		if (fakePlayer == null) {
                fakePlayer = newFakePlayer(ai.ent.worldObj);
            }
    		
    		if (fakePlayer != null && fakePlayer.playerNetServerHandler == null) {
	    		try {
	            	//fakePlayer = newFakePlayer(worldObj);
	            	
	    			if (ai.ent.worldObj.playerEntities.size() > 0)
	    	        {
	    	            if (ai.ent.worldObj.playerEntities.get(0) instanceof EntityPlayerMP)
	    	            {
	    	                fakePlayer.playerNetServerHandler = ((EntityPlayerMP)ai.ent.worldObj.playerEntities.get(0)).playerNetServerHandler;
	    	                fakePlayer.dimension = ((EntityPlayerMP)ai.ent.worldObj.playerEntities.get(0)).dimension;
	    	            }
	    	        }
	    	        else
	    	        {
	    	            //System.out.println("fakeplayer has no netserverhandler, might behave oddly");
	    	        }
	            	
	            	//System.out.println("DEBUG FIX 3: ");
	                //if (fakePlayer != null) ((EntityPlayer)fakePlayer).username = "fakePlayer_";
	                /*if (fakePlayer != null) */((EntityPlayer)fakePlayer).username = "fakePlayer_" + ai.ent.entityId;
	                
	                //if (fakePlayer != null) {
                	inventory = fakePlayer.inventory;
                	sync();
                	
            	    c_CoroAIUtil.playerToCompAILookup.put(fakePlayer.username, ai.entInt);
                    
	                //}
	            } catch (Exception ex) {
	            	ex.printStackTrace();
	            	return;
	            }
    		}
    	//}
    	
    	if (fakePlayer != null) {
	    	if (fakePlayer.isDead) { 
	    		ai.ent.health = 0;
	    	} else {
	    		if (fakePlayer.health > 0) {
	    			ai.ent.deathTime = 0;
	    			sync();
	    		}
	    	}
    	}
    }
    
	public void attackMelee(Entity ent, float dist) {
		// TODO Auto-generated method stub
		this.setCurrentSlot(slot_Melee);
		leftClickItem(ent);
	}

	public void attackRanged(Entity ent, float dist) {
		// TODO Auto-generated method stub
		this.setCurrentSlot(slot_Ranged);
		rightClickItem();
	}
	
	public void leftClickItem(Entity var1) {
    	try {
    		fakePlayer.attackTargetEntityWithCurrentItem(var1);
    		/*if (this.getCurrentEquippedItem() == null) {
    			attackEntityWithNothing(var1);
    		} else {
    			
    		}*/
    	} catch (Exception ex) {
    		//ex.printStackTrace();
    	}
    	ai.ent.swingItem();
    }
	
	public void rightClickItem() {
    	
    	boolean wasFired = true;
    	
		ItemStack itemToUse = inventory.mainInventory[inventory.currentItem];
		if (itemToUse != null) {
			Item ii = itemToUse.getItem();
			if (ii != null) {
				//tropicraft specific ones
				if (itemToUse.getItem() instanceof ItemTropicalFishingRod) {
					((ItemTropicalFishingRod)itemToUse.getItem()).onItemRightClick3(itemToUse, ai.ent.worldObj, this, castingStrength);
					//((ItemTropicalFishingRod)itemToUse.getItem()).onItemRightClick2(itemToUse, worldObj, this, castingStrength);
				} else {
					if (itemToUse.getItem() instanceof ItemTropicraftLeafball) {
						ai.ent.rotationPitch -= 2;
						sync();
					}
					
					
					try {
						itemToUse.useItemRightClick(ai.ent.worldObj, fakePlayer);
						ai.ent.swingItem();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					
					//wasFired = c_CoroAIUtil.AIRightClickHook(this, itemToUse);
					
					
					
					if (itemToUse.getItem() instanceof ItemBow || itemToUse.getItem() instanceof ItemFood) {
						//fakePlayer.itemInUseCount = 0;
						ItemStack itemstack = inventory.mainInventory[inventory.currentItem];
						//not quited used yet, must code item switch lockout code before
						isCharging = true;
						fakePlayer.setItemInUse(itemstack, Item.itemsList[itemstack.itemID].getMaxItemUseDuration(itemstack));
						
						//set to 1, because onUpdate performs -- and checks if == 0
						int inUseCount = 1;
						
						if (itemToUse.getItem() instanceof ItemFood) {
							ItemFood food = (ItemFood) itemToUse.getItem();
							
							//food.onFoodEaten(itemToUse, worldObj, fakePlayer);
							fakePlayer.getFoodStats().addStats(food);
						}
						
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
	}
	
	public void setPrjOwner(int offset) {
    	Entity ent = (Entity)ai.ent.worldObj.loadedEntityList.get(ai.ent.worldObj.loadedEntityList.size()-offset);
    	if (ent instanceof EntityThrowable) {
    		((EntityThrowable) ent).posY += 1.5F;
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
			((EntityArrow) ent).posY += 1F;
			((EntityArrow) ent).shootingEntity = ai.ent;
			//((EntityArrow) ent).doesArrowBelongToPlayer = false;
		} else if (ent instanceof EntityTropicalFishHook) {
			((EntityTropicalFishHook) ent).angler = ai.ent;
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
    	fakePlayer.posY = ai.ent.posY;
    	fakePlayer.posZ = ai.ent.posZ;
    	fakePlayer.prevPosX = ai.ent.prevPosX;
    	fakePlayer.prevPosY = ai.ent.prevPosY;
    	fakePlayer.prevPosZ = ai.ent.prevPosZ;
    	fakePlayer.rotationPitch = ai.ent.rotationPitch;
    	fakePlayer.rotationYaw = ai.ent.rotationYaw;
    	fakePlayer.prevRotationPitch = ai.ent.prevRotationPitch;
    	fakePlayer.prevRotationYaw = ai.ent.prevRotationYaw;
    	
    	c_CoroAIUtil.setHealth(ai.ent, ai.ent.getHealth());
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
        	player = new c_EntityPlayerMPExt(mc, world, "fakePlayer_" + ai.ent.entityId, new ItemInWorldManager(world));
        	inventory = player.inventory;
        } else {
        	//System.out.println("Chunk doesnt exist, cant create fake player, posX: " + posX + " | posZ: " + posZ);
        }
        
        
        
        
        
        //System.out.println("c_CoroAIUtil.playerToAILookup.put(player.username, this); OFF");

        //mc.configManager.netManager
        //player.movementInput = new MovementInputFromOptions(mod_ZombieCraft.mc.gameSettings);
        return player;
    }
}
