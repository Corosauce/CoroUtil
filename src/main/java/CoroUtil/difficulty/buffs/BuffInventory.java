package CoroUtil.difficulty.buffs;

import CoroUtil.difficulty.EquipmentForDifficulty;
import CoroUtil.difficulty.UtilEntityBuffs;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

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

        int inventoryStage = UtilEntityBuffs.getInventoryStageBuff(difficulty);

        EquipmentForDifficulty equipment = UtilEntityBuffs.lookupDifficultyToEquipment.get(inventoryStage);
        if (equipment != null) {
            //allow for original weapon to remain if there was one and we are trying to remove it
            if (equipment.getWeapon() != null) UtilEntityBuffs.setEquipment(ent, EntityEquipmentSlot.MAINHAND, equipment.getWeapon());
            for (int i = 0; i < 4; i++) {
                //TODO: verify 1.10.2 update didnt mess with this, maybe rewrite a bit for new sane slot based system
                if (equipment.getListArmor().size() >= i+1) {
                    UtilEntityBuffs.setEquipment(ent, equipment.getSlotForSlotID(i), equipment.getListArmor().get(i));
                } else {
                    UtilEntityBuffs.setEquipment(ent, equipment.getSlotForSlotID(i), null);

                }
            }

        } else {
            System.out.println("error, couldnt find equipment for difficulty value: " + inventoryStage);
            return false;
        }

        return super.applyBuff(ent, difficulty);
    }
}
