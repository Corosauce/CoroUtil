package CoroUtil.difficulty.buffs;

import CoroUtil.ai.tasks.TaskDigTowardsTarget;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.forge.CULog;
import net.minecraft.entity.EntityCreature;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;

/**
 * Created by Corosus on 1/18/2017.
 */
public class BuffAI_TaskMining extends BuffAI_TaskBase {

    public BuffAI_TaskMining(String buffName, Class task, int taskPriority) {
        super(buffName, task, taskPriority);
    }

    @Override
    public void applyBuffPost(EntityCreature ent, float difficulty) {

        CULog.dbg("applyBuffPost enhancing with digging: " + ent.getName());

        ((PathNavigateGround)ent.getNavigator()).setBreakDoors(false);

        /**
         * These 2 might be redundantly applied during deserialization
         */
        ent.getEntityData().setBoolean(UtilEntityBuffs.dataEntityEnhanced, true);
        ent.getEntityData().setBoolean("CoroAI_HW_GravelDeath", true);

        ItemStack is = ent.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        if (is == null) {
            UtilEntityBuffs.setEquipment(ent, EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
        } else {
            if (is.getItem() == Items.WOODEN_SWORD) {
                UtilEntityBuffs.setEquipment(ent, EntityEquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_PICKAXE));
            } else if (is.getItem() == Items.STONE_SWORD) {
                UtilEntityBuffs.setEquipment(ent, EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_PICKAXE));
            } else if (is.getItem() == Items.IRON_SWORD) {
                UtilEntityBuffs.setEquipment(ent, EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
            } else if (is.getItem() == Items.DIAMOND_SWORD) {
                UtilEntityBuffs.setEquipment(ent, EntityEquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_PICKAXE));
            }
        }

        super.applyBuffPost(ent, difficulty);
    }
}
