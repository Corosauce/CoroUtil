package CoroUtil.difficulty.buffs;

import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.data.cmods.CmodAttributeAttackDamage;
import CoroUtil.difficulty.data.cmods.CmodAttributeHealth;
import CoroUtil.forge.CULog;
import CoroUtil.util.CoroUtilMisc;
import CoroUtil.util.EnumAttribModifierType;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;

public class BuffAttackDamage extends BuffBase {

    @Override
    public String getTagName() {
        return UtilEntityBuffs.dataEntityBuffed_AttackDamage;
    }

    @Override
    public boolean applyBuff(CreatureEntity ent, float difficulty) {

        CmodAttributeAttackDamage cmod = (CmodAttributeAttackDamage)UtilEntityBuffs.getCmodData(ent, getTagName());

        if (cmod != null) {

            //for mobs that didnt have an attack
            if (ent.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
                ent.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3);
            }

            double oldVal = ent.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).get();

            //set base value if we need to
            if (cmod.base_value != -1) {
                ent.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(cmod.base_value);
            }

            double boostMultiply = (/*1F + */difficulty * cmod.difficulty_multiplier);
            ent.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(new AttributeModifier("damage multiplier boost", boostMultiply, EnumAttribModifierType.INCREMENT_MULTIPLY_BASE.ordinal()));

            //cap
            if (cmod.max_value != -1 && ent.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).get() > cmod.max_value) {
                CoroUtilMisc.removeAllModifiers(ent.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE));
                ent.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(cmod.max_value);
            }

            CULog.dbg("mob damage went from " + oldVal + " to " + ent.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).get());
        }
        /*
        double healthBoostMultiply = (difficulty * ConfigHWMonsters.scaleHealth);
        if (healthBoostMultiply > ConfigHWMonsters.scaleHealthMax) {
            healthBoostMultiply = ConfigHWMonsters.scaleHealthMax;
        }
        ent.getAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("health multiplier boost", healthBoostMultiply, EnumAttribModifierType.MULTIPLY_ALL.ordinal()));
        */

        //group with health buff for now...
        //ent.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(difficulty * ConfigHWMonsters.scaleKnockbackResistance);

        return super.applyBuff(ent, difficulty);
    }
}
