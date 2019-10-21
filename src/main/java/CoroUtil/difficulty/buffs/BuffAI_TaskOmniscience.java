package CoroUtil.difficulty.buffs;

import CoroUtil.ai.BehaviorModifier;
import CoroUtil.ai.tasks.EntityAIChaseFromFar;
import CoroUtil.ai.tasks.EntityAINearestAttackablePlayerOmniscience;
import CoroUtil.ai.tasks.TaskDigTowardsTarget;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.forge.CULog;
import net.minecraft.entity.CreatureEntity;

/**
 * Dual AI task user, one a task, one a targetTask
 */
public class BuffAI_TaskOmniscience extends BuffAI_TaskBase {

    public BuffAI_TaskOmniscience(String buffName) {
        super(buffName, EntityAIChaseFromFar.class, -1);
    }

    @Override
    public boolean canApplyBuff(CreatureEntity ent, float difficulty) {
        return super.canApplyBuff(ent, difficulty);
    }

    @Override
    public boolean applyBuff(CreatureEntity ent, float difficulty) {
        return super.applyBuff(ent, difficulty);
    }

    @Override
    public boolean applyBuffImpl(CreatureEntity ent, float difficulty, boolean firstTime) {
        CULog.dbg("trying to enhance with omniscience: " + ent.getName());

        //already confirmed we dont have via usual routes
        UtilEntityBuffs.addGoal(ent, EntityAIChaseFromFar.class, 4, false);

        if (!UtilEntityBuffs.hasTask(ent, EntityAINearestAttackablePlayerOmniscience.class, true)) {

            UtilEntityBuffs.addGoal(ent, EntityAINearestAttackablePlayerOmniscience.class, 10, true);
        }

        return true;
    }
}

