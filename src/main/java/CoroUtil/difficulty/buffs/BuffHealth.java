package CoroUtil.difficulty.buffs;

import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.data.cmods.CmodAttributeHealth;
import CoroUtil.difficulty.data.cmods.CmodInventory;
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

        CmodAttributeHealth cmod = (CmodAttributeHealth)UtilEntityBuffs.getCmodData(ent, getTagName());

        if (cmod != null) {
            /**
             * Lets start by keeping it simple
             * - allow setting base health
             * - allow setting extra health ontop of base health using difficulty * a basic multiplier
             *
             * health = base + (base * difficulty * multiplier)
             *
             * lets follow minecraft attrib rules:
             * - set base health as our base here
             * - apply a multiplier that does exactly above, doable by operation 1 aka INCREMENT_MULTIPLY_BASE
             */

            //set base value if we need to
            if (cmod.base_value != -1) {
                ent.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(cmod.base_value);
            }

            double healthBoostMultiply = (/*1F + */difficulty * cmod.difficulty_multiplier);
            ent.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("health multiplier boost", healthBoostMultiply, EnumAttribModifierType.INCREMENT_MULTIPLY_BASE.ordinal()));
        }
        /*
        double healthBoostMultiply = (difficulty * ConfigHWMonsters.scaleHealth);
        if (healthBoostMultiply > ConfigHWMonsters.scaleHealthMax) {
            healthBoostMultiply = ConfigHWMonsters.scaleHealthMax;
        }
        ent.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("health multiplier boost", healthBoostMultiply, EnumAttribModifierType.MULTIPLY_ALL.ordinal()));
        */

        //group with health buff for now...
        //ent.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(difficulty * ConfigHWMonsters.scaleKnockbackResistance);

        return super.applyBuff(ent, difficulty);
    }
}
