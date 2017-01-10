package CoroUtil.difficulty.buffs;

import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.difficulty.UtilEntityBuffs;
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

        try {
            int xp = ObfuscationReflectionHelper.getPrivateValue(EntityLiving.class, ent, "field_70728_aV", "experienceValue");
            xp += difficulty * 10F;
            ObfuscationReflectionHelper.setPrivateValue(EntityLiving.class, ent, xp, "field_70728_aV", "experienceValue");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return super.applyBuff(ent, difficulty);
    }

    @Override
    public void applyBuffFromReload(EntityCreature ent, float difficulty) {
        applyBuff(ent, difficulty);
    }
}
