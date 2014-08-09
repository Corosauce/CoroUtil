package CoroUtil.inventory;

import CoroUtil.util.CoroUtilItem;

import com.google.common.collect.Multimap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

public class AIInventory {

	//Inventory size of 3 for melee, ranged, and tool
	//Armor will use vanilla stack system for now
	
	public EntityLivingBase entOwner;
	
	public InventoryWrapper inventory;
	
	public int slot_Active = 0;
	public int slot_Count = 3;
	
	//profiled stuff? or standard?
	public int slot_Melee = 0;
	public int slot_Ranged = 1;
	public int slot_Tool = 2;
	
	public AIInventory(EntityLivingBase parEnt) {
		entOwner = parEnt;
		inventory = new InventoryWrapper();
		inventory.invInitData(new ItemStack[slot_Count]);
	}
	
	//server side only
	public FakePlayer getFakePlayer(World parWorld) {
		return FakePlayerFactory.getMinecraft((WorldServer) parWorld);
	}
	
	public void setSlotActive(int parSlot) {
		slot_Active = parSlot;
		syncToClient();
	}
	
	public int getSlotActive() {
		return slot_Active;
	}
	
	public void attackMelee(Entity ent, float dist) {
		setSlotActive(slot_Melee);
		performLeftClick(ent, dist);
	}

	public void attackRanged(Entity ent, float dist) {
		setSlotActive(slot_Ranged);
		/*CoroUtil.faceEntity(fakePlayer, ent, 180, 180);*/
		performRightClick();
	}
	
	public void performLeftClick(Entity ent, float dist) {
		System.out.println("TODO: performLeftClick in AIInventory");
		ItemStack is = inventory.getStackInSlot(slot_Active);
		if (is != null) {
			Item item = is.getItem();
			if (item != null) {
				//dont forget, player code gets the attack attribute, which is constantly updated based on the held item O_o
				//all hitEntity really does is damage the item
				float dmg = 0;
				
				//here, we probably want to do what EntityPlayer.attackTargetEntityWithCurrentItem does, get the attribute
				dmg = CoroUtilItem.getLeftClickDamage(is);
				
				ent.attackEntityFrom(DamageSource.causePlayerDamage(getFakePlayer(entOwner.worldObj)), dmg);
				//item.hitEntity(is, entOwner, par3EntityLivingBase)
			}
		}
		//forge fakeplayer 
	}

	public void performRightClick() {
		System.out.println("TODO: performRightClick in AIInventory");
		ItemStack is = inventory.getStackInSlot(slot_Active);
		if (is != null) {
			Item item = is.getItem();
			if (item != null) {
				item.onItemRightClick(is, entOwner.worldObj, getFakePlayer(entOwner.worldObj));
			}
		}
	}
	
	public void syncToClient() {
		//syncing might not actually be needed given we are setting from our inventory to vanilla EntityLivingBase inventory
		entOwner.setCurrentItemOrArmor(0, inventory.getStackInSlot(0));
	}
	
}
