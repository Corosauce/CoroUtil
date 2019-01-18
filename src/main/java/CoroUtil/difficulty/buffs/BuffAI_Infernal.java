package CoroUtil.difficulty.buffs;

import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.data.cmods.CmodAIInfernal;
import CoroUtil.util.CoroUtilCrossMod;
import net.minecraft.entity.EntityCreature;

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

        CmodAIInfernal cmod = (CmodAIInfernal)UtilEntityBuffs.getCmodData(ent, getTagName());

        if (cmod.randomly_choose_count > 0) {

            /*int countTry = 0;
            List<String> modifiersToUse = new ArrayList<>();

            while (countTry < cmod.count) {

                int safetyBail = 0;
                int safetyBailMax = 15;



                countTry++;
            }*/

            /*String infernalmods = "";
            for (String mod : cmod.modifiers) {
                infernalmods += mod + " ";
            }
            CoroUtilCrossMod.infernalMobs_AddModifiers(ent, infernalmods);*/

            int count = (int)((float)cmod.randomly_choose_count * cmod.difficulty_multiplier * difficulty);
            if (cmod.randomly_choose_count_max != -1 && count > cmod.randomly_choose_count_max) {
                count = cmod.randomly_choose_count_max;
            }
            CoroUtilCrossMod.infernalMobs_AddRandomModifiers(ent, cmod.modifiers, count);

        }
        //CoroUtilCrossMod.infernalMobs_AddRandomModifiers(ent, getBuffsForDifficulty(difficulty));

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
