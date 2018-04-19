package CoroUtil.difficulty.buffs;

import CoroUtil.ai.BehaviorModifier;
import CoroUtil.ai.tasks.EntityAITaskAntiAir;
import CoroUtil.ai.tasks.EntityAITaskEnhancedCombat;
import CoroUtil.ai.tasks.TaskDigTowardsTarget;
import CoroUtil.difficulty.UtilEntityBuffs;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIZombieAttack;

/**
 * Created by Corosus on 1/9/2017.
 */
public class BuffAI_TaskBase extends BuffBase {

    private String buffName;
    private Class task;
    private Class taskToReplace;
    private int taskPriority;
    private float minRequiredDifficulty = 0;
    private boolean isTargetTask = false;

    public BuffAI_TaskBase(String buffName, Class task, int taskPriority, Class taskToReplace) {
        this(buffName, task, taskPriority);
        this.taskToReplace = taskToReplace;
    }

    public BuffAI_TaskBase(String buffName, Class task, int taskPriority) {
        this.buffName = buffName;
        this.task = task;
        this.taskPriority = taskPriority;
    }

    @Override
    public String getTagName() {
        return buffName;
    }

    @Override
    public float getMinRequiredDifficulty() {
        return minRequiredDifficulty;
    }

    public void setMinRequiredDifficulty(float minRequiredDifficulty) {
        this.minRequiredDifficulty = minRequiredDifficulty;
    }

    @Override
    public boolean applyBuff(EntityCreature ent, float difficulty) {

        if (applyBuffImpl(ent, difficulty, true)) {
            return super.applyBuff(ent, difficulty);
        } else {
            return false;
        }
    }

    @Override
    public boolean canApplyBuff(EntityCreature ent, float difficulty) {
        return !UtilEntityBuffs.hasTask(ent, task, isTargetTask);
    }

    @Override
    public void applyBuffFromReload(EntityCreature ent, float difficulty) {

        //probably redundant if statement, added for safety
        if (canApplyBuff(ent, difficulty)) {
            this.applyBuffImpl(ent, difficulty, false);
        }

    }

    public boolean applyBuffImpl(EntityCreature ent, float difficulty, boolean firstTime) {
        if (taskToReplace != null) {
            if (BehaviorModifier.replaceTaskIfMissing(ent, taskToReplace, task, taskPriority, isTargetTask)) {
                return true;
            } else {
                return false;
            }
        } else {
            UtilEntityBuffs.addTask(ent, task, taskPriority, isTargetTask);
        }

        return true;
    }

    public boolean isTargetTask() {
        return isTargetTask;
    }

    public void setTargetTask(boolean targetTask) {
        isTargetTask = targetTask;
    }
}
