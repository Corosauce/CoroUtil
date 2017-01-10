package CoroUtil.difficulty.buffs;

import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.difficulty.UtilEntityBuffs;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;

/**
 * Created by Corosus on 1/9/2017.
 */
public class BuffSpeed extends BuffBase {

    @Override
    public String getTagName() {
        return UtilEntityBuffs.dataEntityBuffed_Speed;
    }

    @Override
    public boolean applyBuff(EntityCreature ent, float difficulty) {

        /**
         * TODO: use unique persistant UUID for all mods that want to apply dangerously overlapping speed buffs
         * - zombie awareness
         * - invasion
         * - monsters
         */


        double curSpeed = ent.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
        //avoid retardedly fast speeds
        if (curSpeed < UtilEntityBuffs.speedCap) {
            double speedBoost = (Math.min(ConfigHWMonsters.scaleSpeedCap, difficulty * ConfigHWMonsters.scaleSpeed));
            //debug += "speed % " + speedBoost;
            ent.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(new AttributeModifier("speed multiplier boost", speedBoost, 2));
        }

        return super.applyBuff(ent, difficulty);
    }
}
