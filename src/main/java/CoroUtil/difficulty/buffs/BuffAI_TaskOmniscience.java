package CoroUtil.difficulty.buffs;

import CoroUtil.ai.BehaviorModifier;
import CoroUtil.ai.tasks.EntityAIChaseFromFar;
import CoroUtil.ai.tasks.EntityAINearestAttackablePlayerOmniscience;
import CoroUtil.ai.tasks.TaskDigTowardsTarget;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.forge.CULog;
import net.minecraft.entity.EntityCreature;

/**
 * Dual AI task user, one a task, one a targetTask
 */
public class BuffAI_TaskOmniscience extends BuffAI_TaskBase {

    public BuffAI_TaskOmniscience(String buffName) {
        super(buffName, EntityAIChaseFromFar.class, -1);
    }

    @Override
    public boolean canApplyBuff(EntityCreature ent, float difficulty) {
        return super.canApplyBuff(ent, difficulty);
    }

    @Override
    public boolean applyBuff(EntityCreature ent, float difficulty) {
        return super.applyBuff(ent, difficulty);
    }

    @Override
    public boolean applyBuffImpl(EntityCreature ent, float difficulty, boolean firstTime) {
        CULog.dbg("trying to enhance with omniscience: " + ent.getName());

        //already confirmed we dont have via usual routes
        UtilEntityBuffs.addTask(ent, EntityAIChaseFromFar.class, 4, false);

        if (!UtilEntityBuffs.hasTask(ent, EntityAINearestAttackablePlayerOmniscience.class, true)) {

            UtilEntityBuffs.addTask(ent, EntityAINearestAttackablePlayerOmniscience.class, 10, true);
        }

        return true;
    }
}
