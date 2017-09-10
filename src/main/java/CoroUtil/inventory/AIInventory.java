package CoroUtil.inventory;

import CoroUtil.bt.IBTAgent;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.List;

public class AIInventory {

	//Inventory size of 3 for melee, ranged, and tool
	//Armor will use vanilla stack system for now

	//custom fakePlayer for Comrade support
	public GameProfile playerProfile = null;
	//public FakePlayer fakePlayer = null;

	public EntityLivingBase entOwner;

	public InventoryWrapper inventory;

	public int slot_Active = 0;
	public int slot_Count = 3 + 10;

	//profiled stuff? or standard?
	public static int slot_Melee = 0;
	public static int slot_Ranged = 1;
	public static int slot_Tool = 2;

	//because cross compatibility
	//public EntityTropicalFishHook fishEntity;

	public boolean grabItems = false;

	public AIInventory(EntityLivingBase parEnt) {
		entOwner = parEnt;
		inventory = new InventoryWrapper();
		inventory.invInitData(new ItemStack[slot_Count]);
	}

	public static AIInventory getInventory(EntityLivingBase parEntSource) {
		if (parEntSource instanceof IBTAgent) {
			return ((IBTAgent)parEntSource).getAIBTAgent().entInv;
		}
		return null;
	}

	//server side only
	/*public FakePlayer getFakePlayer(World parWorld) {
		if (fakePlayer == null) {
			playerProfile = new GameProfile(UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77"), "fakePlayer" + entOwner.getEntityId()"[Minecraft]");
			fakePlayer = FakePlayerFactory.get((WorldServer) parWorld, playerProfile);
			if (entOwner instanceof IBTAgent) {
				System.out.println("TODO: add lookup for fakeplayer to AIBTAgent");//((IBTAgent)entOwner).getAIBTAgent().entInv;
			}

			//need username to be "fakePlayer" + this.getEntityId();
		}

		//sync fakeplayer to real ent pos
		fakePlayer.setPosition(entOwner.posX, entOwner.posY, entOwner.posZ);
		fakePlayer.rotationPitch = entOwner.rotationPitch;
		fakePlayer.rotationYaw = entOwner.rotationYaw;
		fakePlayer.rotationYawHead = entOwner.rotationYawHead;

		return fakePlayer;
	}*/

	public void setSlotContents(int parSlot, ItemStack parStack) {
		inventory.setInventorySlotContents(parSlot, parStack);
		syncToClient();
	}

	public void setSlotActive(int parSlot) {
		int oldSlot = slot_Active;
		slot_Active = parSlot;
		if (slot_Active != oldSlot) syncToClient();
	}

	public int getSlotActive() {
		return slot_Active;
	}

	public ItemStack getActiveItem() {
		return inventory.getStackInSlot(slot_Active);
	}

	/*public void attackMelee(Entity ent, float dist) {
		setSlotActive(slot_Melee);
		performLeftClick(ent, dist);
	}

	public void attackRanged(Entity ent, float dist) {
		setSlotActive(slot_Ranged);
		//CoroUtil.faceEntity(fakePlayer, ent, 180, 180);
		performRightClick();
	}*/

	public NBTTagCompound nbtWrite() {
		NBTTagCompound nbt = new NBTTagCompound();

		NBTTagList nbttaglist = new NBTTagList();
		NBTTagCompound nbttagcompound1;

        for (int i = 0; i < inventory.invList.length; ++i)
        {
            nbttagcompound1 = new NBTTagCompound();

            if (inventory.invList[i] != null)
            {
            	inventory.invList[i].writeToNBT(nbttagcompound1);
            }

            nbttaglist.appendTag(nbttagcompound1);
        }

        nbt.setTag("listInv", nbttaglist);

		return nbt;
	}

	public void nbtRead(NBTTagCompound parNBT) {
		NBTTagList nbttaglist;
        int i;

        if (parNBT.hasKey("listInv", 9))
        {
            nbttaglist = parNBT.getTagList("listInv", 10);

            for (i = 0; i < inventory.invList.length; ++i)
            {
            	inventory.invList[i] = new ItemStack(nbttaglist.getCompoundTagAt(i));
            }
        }
	}/*

	public void performLeftClick(Entity ent, float dist) {
		//System.out.println("CHECK: performLeftClick in AIInventory");
		ItemStack is = getActiveItem();//inventory.getStackInSlot(slot_Active);
		if (is != null) {
			Item item = is.getItem();
			if (item != null) {
				//dont forget, player code gets the attack attribute, which is constantly updated based on the held item O_o
				//all hitEntity really does is damage the item
				float dmg = 0;

				//here, we probably want to do what EntityPlayer.attackTargetEntityWithCurrentItem does, get the attribute
				dmg = CoroUtilItem.getLeftClickDamage(is);

				ent.attackEntityFrom(DamageSource.causePlayerDamage(getFakePlayer(entOwner.world)), dmg);
				//item.hitEntity(is, entOwner, par3EntityLivingBase)
			}
		}
		//forge fakeplayer
	}

	public void performRightClick() {
		//System.out.println("CHECK: performRightClick in AIInventory");
		ItemStack is = getActiveItem();//inventory.getStackInSlot(slot_Active);
		if (is != null) {
			Item item = is.getItem();
			if (item != null) {
				//special fishing exception
				if (item instanceof ItemTropicalFishingRod) {
					((ItemTropicalFishingRod)item).onItemRightClickSpecial(is, entOwner.world, getFakePlayer(entOwner.world), entOwner);
				} else {
					item.onItemRightClick(is, entOwner.world, getFakePlayer(entOwner.world));
				//}
			}
		}
	}*/

	public void syncToClient() {
		//syncing might not actually be needed given we are setting from our inventory to vanilla EntityLivingBase inventory
		//TODO: 1.10 verify if needed
		//entOwner.setCurrentItemOrArmor(0, inventory.getStackInSlot(getSlotActive()));
	}

	public void tick() {
		tickItemPickupScan();
	}

	public void tickItemPickupScan() {
    	List var3 = entOwner.world.getEntitiesWithinAABBExcludingEntity(entOwner, entOwner.getEntityBoundingBox().expand(2.0D, 1.0D, 2.0D));

        if(var3 != null) {
            for(int var4 = 0; var4 < var3.size(); ++var4) {
                Entity var5 = (Entity)var3.get(var4);

                if(!var5.isDead) {
                	if (/*(grabXP && (var5 instanceof EntityXPOrb)) || */(grabItems && (var5 instanceof EntityItem))) {
                		collideWithItem((EntityItem)var5);
                	}
                }
            }
        }
    }

	public void collideWithItem(EntityItem parItem) {
		ItemStack is = parItem.getItem();
		inventory.addItemStackToInventory(is);
		if (is.getCount() <= 0)
        {
			parItem.setDead();
        }
	}

	public void cleanup() {
		//if (fakePlayer != null) OldUtil.playerToCompAILookup.remove(CoroUtilEntity.getName(fakePlayer));
		entOwner = null;
    	//inventory = null; //playerKillEvent needs this, race condition?
    	//fishEntity = null;
    	playerProfile = null;
    	//fakePlayer = null;
    }
}
