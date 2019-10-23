package CoroUtil.difficulty.buffs;

import CoroUtil.ai.BehaviorModifier;
import CoroUtil.ai.goalSelector.EntityAITaskAntiAir;
import CoroUtil.ai.goalSelector.EntityAITaskEnhancedCombat;
import CoroUtil.ai.goalSelector.TaskDigTowardsTarget;
import CoroUtil.difficulty.UtilEntityBuffs;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;

/**
 * Created by Corosus on 1/9/2017.
 */
public class BuffAI_TaskBase extends BuffBase {

    protected String buffName;
    protected Class task;
    protected Class taskToReplace;
    protected int taskPriority;
    protected float minRequiredDifficulty = 0;
    protected boolean isTargetTask = false;

    /**
     * To enable use of multiple enableable buffs that are used within the same AI task, eg counter attack and lunging
     * First one run will set the task, second one will just skip over if its already added and assume all good
     */
    protected boolean allowRedundantAttempts = false;

    public BuffAI_TaskBase(String buffName, Class task, int taskPriority, Class taskToReplace) {
        this(buffName, task, taskPriority);
        this.taskToReplace = taskToReplace;
    }

    public BuffAI_TaskBase(String buffName, Class task, int taskPriority) {
        this.buffName = buffName;
        this.task = task;
        this.taskPriority = taskPriority;
    }

    public BuffAI_TaskBase setAllowRedundantAttempts() {
        allowRedundantAttempts = true;
        return this;
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
    public boolean applyBuff(CreatureEntity ent, float difficulty) {

        if (applyBuffImpl(ent, difficulty, true) || allowRedundantAttempts) {
            return super.applyBuff(ent, difficulty);
        } else {
            return false;
        }
    }

    @Override
    public boolean canApplyBuff(CreatureEntity ent, float difficulty) {
        return !UtilEntityBuffs.hasTask(ent, task, isTargetTask) || allowRedundantAttempts;
    }

    @Override
    public void applyBuffFromReload(CreatureEntity ent, float difficulty) {

        //probably redundant if statement, added for safety
        //no longer redundant due to addition of allowRedundantAttempts
        if (canApplyBuff(ent, difficulty)) {
            this.applyBuffImpl(ent, difficulty, false);
        }

    }

    public boolean applyBuffImpl(CreatureEntity ent, float difficulty, boolean firstTime) {
        if (taskToReplace != null) {
            if (BehaviorModifier.replaceTaskIfMissing(ent, taskToReplace, task, taskPriority, isTargetTask)) {
                return true;
            } else {
                return false;
            }
        } else {
            //added since addition of allowRedundantAttempts feature
            if (!UtilEntityBuffs.hasTask(ent, task, isTargetTask)) {
                return UtilEntityBuffs.addGoal(ent, task, taskPriority, isTargetTask);
            } else {
                return false;
            }
        }
    }

    public boolean isTargetTask() {
        return isTargetTask;
    }

    public void setTargetTask(boolean targetTask) {
        isTargetTask = targetTask;
    }
}
