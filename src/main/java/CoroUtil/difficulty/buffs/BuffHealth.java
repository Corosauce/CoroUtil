package CoroUtil.difficulty.buffs;

import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.difficulty.UtilEntityBuffs;
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
        ent.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("health multiplier boost", healthBoostMultiply, 2));

        //group with health buff for now...
        ent.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(difficulty * ConfigHWMonsters.scaleKnockbackResistance);

        return super.applyBuff(ent, difficulty);
    }
}
