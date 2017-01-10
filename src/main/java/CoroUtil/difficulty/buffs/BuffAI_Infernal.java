package CoroUtil.difficulty.buffs;

import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.util.CoroUtilCrossMod;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;

/**
 * Created by Corosus on 1/9/2017.
 */
public class BuffAI_Infernal extends BuffBase {

    @Override
    public String getTagName() {
        return UtilEntityBuffs.dataEntityBuffed_AI_Infernal;
    }

    @Override
    public boolean applyBuff(EntityCreature ent, float difficulty) {

        //TODO: how many modifiers do we add? based on difficulty i guess, x2!
        CoroUtilCrossMod.infernalMobs_AddRandomModifiers(ent, (int)(difficulty * 2D));

        return super.applyBuff(ent, difficulty);
    }

    @Override
    public boolean canApplyBuff(EntityCreature ent, float difficulty) {
        return CoroUtilCrossMod.hasInfernalMobs();
    }

    @Override
    public void applyBuffFromReload(EntityCreature ent, float difficulty) {

        //TODO: see if we can just give infernal buffs the correct way so infernal mobs manages its own restoring of its modifiers

        super.applyBuffFromReload(ent, difficulty);
    }
}
