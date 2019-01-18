package CoroUtil.difficulty.data.conditions;

import CoroUtil.difficulty.data.DataCondition;

/**
 * Created by Corosus on 2/26/2017.
 */
public class ConditionInvasionNumber extends DataCondition {
    public int min = -1;
    public int max = -1;

    @Override
    public String toString() {
        return "wave range: " + min + " to " + max;
    }
}
