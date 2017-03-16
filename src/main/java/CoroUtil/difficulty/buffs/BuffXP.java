package CoroUtil.difficulty.buffs;

import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.data.cmods.CmodAttributeHealth;
import CoroUtil.difficulty.data.cmods.CmodXP;
import CoroUtil.util.EnumAttribModifierType;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * Created by Corosus on 1/9/2017.
 */
public class BuffXP extends BuffBase {

    @Override
    public String getTagName() {
        return UtilEntityBuffs.dataEntityBuffed_XP;
    }

    @Override
    public boolean applyBuff(EntityCreature ent, float difficulty) {

        CmodXP cmod = (CmodXP)UtilEntityBuffs.getCmodData(ent, getTagName());

        if (cmod != null) {
            //set base value if we need to
            if (cmod.base_value != -1) {
                ent.experienceValue = (int)cmod.base_value;
            }
            double extraMultiplier = (/*1F + */difficulty * cmod.difficulty_multiplier);
            ent.experienceValue += (int)((double)ent.experienceValue * extraMultiplier);
        }



        return super.applyBuff(ent, difficulty);
    }

    @Override
    public void applyBuffFromReload(EntityCreature ent, float difficulty) {
        applyBuff(ent, difficulty);
    }
}
