package CoroUtil.difficulty.buffs;

import CoroUtil.difficulty.EquipmentForDifficulty;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.data.cmods.CmodInventory;
import CoroUtil.forge.CULog;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
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
    public boolean applyBuff(CreatureEntity ent, float difficulty) {

        CmodInventory cmod = (CmodInventory)UtilEntityBuffs.getCmodData(ent, getTagName());

        if (cmod != null) {
            EquipmentForDifficulty equipment = UtilEntityBuffs.getEquipmentItemsFromData(cmod);

            //allow for original weapon to remain if there was one and we are trying to remove it
            if (equipment.getWeapon() != null)
                UtilEntityBuffs.setEquipment(ent, EquipmentSlotType.MAINHAND, equipment.getWeapon());

            if (equipment.getWeaponOffhand() != null)
                UtilEntityBuffs.setEquipment(ent, EquipmentSlotType.OFFHAND, equipment.getWeaponOffhand());

            for (ItemStack itemStack : equipment.getListArmor()) {
                if (itemStack.getItem() instanceof ArmorItem) {
                    ArmorItem itemArmor = (ArmorItem) itemStack.getItem();

                    UtilEntityBuffs.setEquipment(ent, itemArmor.armorType, itemStack);
                }
            }

            //update skeleton AI for melee/bow
            if (ent instanceof AbstractSkeletonEntity) {
                ((AbstractSkeletonEntity) ent).setCombatTask();
            }
        } else {
            CULog.err("error, couldnt find cmod data for entity, name: " + getTagName());
            return false;
        }

        return super.applyBuff(ent, difficulty);
    }
}
