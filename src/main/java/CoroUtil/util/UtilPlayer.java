package CoroUtil.util;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

public class UtilPlayer {

	public static int getPlayerRating(EntityPlayer player, boolean calculateWeapon) {
    	
		float armorValue = 0;
		float bestWeaponValue = 0;
		boolean hasGlove = false;
		
		EntityPlayer entP = player;//tryGetCursedPlayer(cursedPlayers.get(i));
		
		for (int armorIndex = 0; armorIndex < 4; armorIndex++) {
			ItemStack stack = entP.inventory.armorInventory.get(armorIndex);
			
			if (!stack.isEmpty()) {
				//testing enchantment debug
				/*if (stack.getEnchantmentTagList() == null || stack.getEnchantmentTagList().hasNoTags()) {
					stack.addEnchantment(Enchantment.protection, 5);
				}*/
				
				if (stack.getItem() instanceof ItemArmor) {
					armorValue += ((ItemArmor)stack.getItem()).damageReduceAmount;
					
				}
			}
		}
		
		//randomization appears to be in play for this
		armorValue += EnchantmentHelper.getEnchantmentModifierDamage(entP.getArmorInventoryList(), DamageSource.GENERIC);
		
		//new plan here for 1.6
		//remove attrib from ent for current item
		//for each item
		//- remove attrib from prev (????) - unneeded step
		//- apply attrib
		//- get damage
		//- remove attrib to reset this part
		//finally readd current weapon attrib onto ent to undo any weird manip
		
		//initial removal of current weap attrib
		if (calculateWeapon) {
			ItemStack itemstack = entP.inventory.getCurrentItem();
			if (!itemstack.isEmpty()) entP.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
			
			for (int slotIndex = 0; slotIndex < entP.inventory.mainInventory.size(); slotIndex++) {
				if (!entP.inventory.mainInventory.get(slotIndex).isEmpty()) {
					//if (entP.inventory.mainInventory[slotIndex].getItem() == ParticleMan.itemGlove) hasGlove = true;
					
					itemstack = entP.inventory.mainInventory.get(slotIndex);
	
	                if (!itemstack.isEmpty())
	                {
	                	//add attrib
	                	entP.getAttributeMap().applyAttributeModifiers(itemstack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
	                	
	                	//temp enchant test
	                	/*if (itemstack.getItem() instanceof ItemSword) {
		                	if (itemstack.getEnchantmentTagList() == null || itemstack.getEnchantmentTagList().hasNoTags()) {
		                		itemstack.addEnchantment(Enchantment.sharpness, 5);
		                	}
	                	}*/
					}
	                
	                //get val
	                float f = (float)entP.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
	                float f1 = 0.0F;
	
	                if (entP instanceof EntityLivingBase)
	                {
	                	if (!itemstack.isEmpty()) {
	                    	//these need to have a target entity passed to them, hmmmmmmm, use own reference for now like old code apparently did
	                        f1 = EnchantmentHelper.getModifierForCreature(itemstack, EnumCreatureAttribute.UNDEFINED);
	                        //i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase)par1Entity);
	                	}
	                }
	                
	                float dmg = f + f1;
	
					if (!itemstack.isEmpty())
	                {
						//remove attrib
						entP.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
	                }
					
	                if (dmg > bestWeaponValue) {
						bestWeaponValue = dmg;
					}
				}
			}
			
			//readd of current weapon attrib
			itemstack = entP.inventory.getCurrentItem();
			if (!itemstack.isEmpty()) entP.getAttributeMap().applyAttributeModifiers(itemstack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
		}
		
		//System.out.println("calculated bestWeaponValue: " + bestWeaponValue);
		//WorldDirectorMultiDim.getPlayerNBT(CoroUtilEntity.getName(entP)).setInteger("HWPlayerRating", (int)(armorValue + bestWeaponValue + (hasGlove ? 20 : 0)));
		
		return (int)(armorValue + bestWeaponValue + (hasGlove ? 20 : 0));
	}
	
}
