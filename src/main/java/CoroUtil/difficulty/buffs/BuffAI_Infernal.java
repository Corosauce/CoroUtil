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

        CoroUtilCrossMod.infernalMobs_AddRandomModifiers(ent, getBuffsForDifficulty(difficulty));

        return super.applyBuff(ent, difficulty);
    }

    @Override
    public boolean canApplyBuff(EntityCreature ent, float difficulty) {
        return CoroUtilCrossMod.hasInfernalMobs();
    }

    @Override
    public void applyBuffFromReload(EntityCreature ent, float difficulty) {

        //infernal mobs own system should be restoring it correctly now

        super.applyBuffFromReload(ent, difficulty);
    }

    /**
     * For every 0.1 of difficulty, add an infernal modifier
     *
     * @param difficulty
     * @return
     */
    public int getBuffsForDifficulty(float difficulty) {
        return (int)(difficulty * 10D);
    }
}
