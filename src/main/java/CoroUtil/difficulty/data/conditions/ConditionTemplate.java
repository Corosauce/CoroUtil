package CoroUtil.difficulty.data.conditions;

import CoroUtil.difficulty.data.DataCondition;

/**
 * Created by Corosus on 2/26/2017.
 */
public class ConditionTemplate extends DataCondition {
    public String template;

    @Override
    public String toString() {
        return super.toString() + ": " + template;
    }
}
