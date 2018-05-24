package CoroUtil.difficulty.buffs;

import CoroUtil.difficulty.EquipmentForDifficulty;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.data.cmods.CmodInventory;
import CoroUtil.forge.CULog;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * Created by Corosus on 1/9/2017.
 */
public class BuffInventory extends BuffBase {

    @Override
    public String getTagName() {
        return UtilEntityBuffs.dataEntityBuffed_Inventory;
    }

    @Override
    public boolean applyBuff(EntityCreature ent, float difficulty) {

        CmodInventory cmod = (CmodInventory)UtilEntityBuffs.getCmodData(ent, getTagName());

        if (cmod != null) {
            EquipmentForDifficulty equipment = UtilEntityBuffs.getEquipmentItemsFromData(cmod);

            //allow for original weapon to remain if there was one and we are trying to remove it
            if (equipment.getWeapon() != null)
                UtilEntityBuffs.setEquipment(ent, EntityEquipmentSlot.MAINHAND, equipment.getWeapon());

            if (equipment.getWeaponOffhand() != null)
                UtilEntityBuffs.setEquipment(ent, EntityEquipmentSlot.OFFHAND, equipment.getWeaponOffhand());

            for (ItemStack itemStack : equipment.getListArmor()) {
                if (itemStack.getItem() instanceof ItemArmor) {
                    ItemArmor itemArmor = (ItemArmor) itemStack.getItem();

                    UtilEntityBuffs.setEquipment(ent, itemArmor.getEquipmentSlot(), itemStack);
                }
            }

            //update skeleton AI for melee/bow
            if (ent instanceof AbstractSkeleton) {
                ((AbstractSkeleton) ent).setCombatTask();
            }
        } else {
            CULog.err("error, couldnt find cmod data for entity, name: " + getTagName());
            return false;
        }

        return super.applyBuff(ent, difficulty);
    }
}
