package CoroUtil.difficulty.buffs;

import CoroUtil.ai.tasks.TaskDigTowardsTarget;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.forge.CULog;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Items;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.GroundPathNavigator;

/**
 * Created by Corosus on 1/18/2017.
 */
public class BuffAI_TaskMining extends BuffAI_TaskBase {

    public BuffAI_TaskMining(String buffName, Class task, int taskPriority) {
        super(buffName, task, taskPriority);
    }

    @Override
    public void applyBuffPost(CreatureEntity ent, float difficulty) {

        CULog.dbg("applyBuffPost enhancing with digging: " + ent.getName());

        if (ent.getNavigator() instanceof GroundPathNavigator) {
            ((GroundPathNavigator)ent.getNavigator()).setBreakDoors(false);
        }

        /**
         * These 2 might be redundantly applied during deserialization
         */
        ent.getEntityData().putBoolean(UtilEntityBuffs.dataEntityEnhanced, true);
        ent.getEntityData().putBoolean("CoroAI_HW_GravelDeath", true);

        ItemStack is = ent.getItemStackFromSlot(EquipmentSlotType.MAINHAND);
        if (is == null) {
            UtilEntityBuffs.setEquipment(ent, EquipmentSlotType.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
        } else {
            if (is.getItem() == Items.WOODEN_SWORD) {
                UtilEntityBuffs.setEquipment(ent, EquipmentSlotType.MAINHAND, new ItemStack(Items.WOODEN_PICKAXE));
            } else if (is.getItem() == Items.STONE_SWORD) {
                UtilEntityBuffs.setEquipment(ent, EquipmentSlotType.MAINHAND, new ItemStack(Items.STONE_PICKAXE));
            } else if (is.getItem() == Items.IRON_SWORD) {
                UtilEntityBuffs.setEquipment(ent, EquipmentSlotType.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
            } else if (is.getItem() == Items.DIAMOND_SWORD) {
                UtilEntityBuffs.setEquipment(ent, EquipmentSlotType.MAINHAND, new ItemStack(Items.DIAMOND_PICKAXE));
            }
        }

        super.applyBuffPost(ent, difficulty);
    }
}

