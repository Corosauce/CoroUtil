package CoroUtil.difficulty.buffs;

import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.util.EnumAttribModifierType;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;

/**
 * Created by Corosus on 1/9/2017.
 */
public class BuffHealth extends BuffBase {

    @Override
    public String getTagName() {
        return UtilEntityBuffs.dataEntityBuffed_Health;
    }

    @Override
    public boolean applyBuff(EntityCreature ent, float difficulty) {
        double healthBoostMultiply = (/*1F + */difficulty * ConfigHWMonsters.scaleHealth);
        if (healthBoostMultiply > ConfigHWMonsters.scaleHealthMax) {
            healthBoostMultiply = ConfigHWMonsters.scaleHealthMax;
        }
        ent.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("health multiplier boost", healthBoostMultiply, EnumAttribModifierType.MULTIPLY_ALL.ordinal()));

        //group with health buff for now...
        ent.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(difficulty * ConfigHWMonsters.scaleKnockbackResistance);

        return super.applyBuff(ent, difficulty);
    }
}
