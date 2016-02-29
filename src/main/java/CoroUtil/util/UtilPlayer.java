package CoroUtil.util;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

public class UtilPlayer {

	public static int getPlayerRating(EntityPlayer player) {
    	
		float armorValue = 0;
		float bestWeaponValue = 0;
		boolean hasGlove = false;
		
		EntityPlayer entP = player;//tryGetCursedPlayer(cursedPlayers.get(i));
		
		for (int armorIndex = 0; armorIndex < 4; armorIndex++) {
			ItemStack stack = entP.inventory.armorInventory[armorIndex];
			
			if (stack != null) {
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
		armorValue += EnchantmentHelper.getEnchantmentModifierDamage(entP.inventory.armorInventory, DamageSource.generic);
		
		//new plan here for 1.6
		//remove attrib from ent for current item
		//for each item
		//- remove attrib from prev (????) - unneeded step
		//- apply attrib
		//- get damage
		//- remove attrib to reset this part
		//finally readd current weapon attrib onto ent to undo any weird manip
		
		//initial removal of current weap attrib
		ItemStack itemstack = entP.inventory.getCurrentItem();
		if (itemstack != null) entP.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers());
		
		for (int slotIndex = 0; slotIndex < entP.inventory.mainInventory.length; slotIndex++) {
			if (entP.inventory.mainInventory[slotIndex] != null) {
				//if (entP.inventory.mainInventory[slotIndex].getItem() == ParticleMan.itemGlove) hasGlove = true;
				
				itemstack = entP.inventory.mainInventory[slotIndex];

                if (itemstack != null)
                {
                	//add attrib
                	entP.getAttributeMap().applyAttributeModifiers(itemstack.getAttributeModifiers());
                	
                	//temp enchant test
                	/*if (itemstack.getItem() instanceof ItemSword) {
	                	if (itemstack.getEnchantmentTagList() == null || itemstack.getEnchantmentTagList().hasNoTags()) {
	                		itemstack.addEnchantment(Enchantment.sharpness, 5);
	                	}
                	}*/
				}
                
                //get val
                float f = (float)entP.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
                float f1 = 0.0F;

                if (entP instanceof EntityLivingBase)
                {
                	if (itemstack != null) {
                    	//these need to have a target entity passed to them, hmmmmmmm, use own reference for now like old code apparently did
                        f1 = EnchantmentHelper.func_152377_a(itemstack, EnumCreatureAttribute.UNDEFINED);
                        //i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase)par1Entity);
                	}
                }
                
                float dmg = f + f1;

				if (itemstack != null)
                {
					//remove attrib
					entP.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers());
                }
				
                if (dmg > bestWeaponValue) {
					bestWeaponValue = dmg;
				}
			}
		}
		
		//readd of current weapon attrib
		itemstack = entP.inventory.getCurrentItem();
		if (itemstack != null) entP.getAttributeMap().applyAttributeModifiers(itemstack.getAttributeModifiers());
		
		//System.out.println("calculated bestWeaponValue: " + bestWeaponValue);
		//WorldDirectorMultiDim.getPlayerNBT(CoroUtilEntity.getName(entP)).setInteger("HWPlayerRating", (int)(armorValue + bestWeaponValue + (hasGlove ? 20 : 0)));
		
		return (int)(armorValue + bestWeaponValue + (hasGlove ? 20 : 0));
	}
	
	public static int getBestPlayerRatingPossible() {
		//diamond armor
		int bestArmor = 20;
		//protection 5 on diamond armor (there is randomization)
		int bestArmorEnchant = 25;
		int bestWeapon = 8;
		//6.25 for sharpness 5
		int bestWeaponEnchant = 6;
		
		//best for vanilla stuff is about 60?
		int bestVal = bestArmor + bestArmorEnchant + bestWeapon + bestWeaponEnchant;
		return bestVal;
	}
	
}
