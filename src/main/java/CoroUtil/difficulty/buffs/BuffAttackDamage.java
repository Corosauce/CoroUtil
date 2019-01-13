package CoroUtil.difficulty.buffs;

import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.data.cmods.CmodAttributeAttackDamage;
import CoroUtil.difficulty.data.cmods.CmodAttributeHealth;
import CoroUtil.forge.CULog;
import CoroUtil.util.EnumAttribModifierType;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;

public class BuffAttackDamage extends BuffBase {

    @Override
    public String getTagName() {
        return UtilEntityBuffs.dataEntityBuffed_AttackDamage;
    }

    @Override
    public boolean applyBuff(EntityCreature ent, float difficulty) {

        CmodAttributeAttackDamage cmod = (CmodAttributeAttackDamage)UtilEntityBuffs.getCmodData(ent, getTagName());

        if (cmod != null) {

            //for mobs that didnt have an attack
            if (ent.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
                ent.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3);
            }

            double oldVal = ent.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();

            //set base value if we need to
            if (cmod.base_value != -1) {
                ent.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(cmod.base_value);
            }

            double boostMultiply = (/*1F + */difficulty * cmod.difficulty_multiplier);
            ent.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(new AttributeModifier("damage multiplier boost", boostMultiply, EnumAttribModifierType.INCREMENT_MULTIPLY_BASE.ordinal()));

            CULog.dbg("mob damage went from " + oldVal + " to " + ent.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
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
