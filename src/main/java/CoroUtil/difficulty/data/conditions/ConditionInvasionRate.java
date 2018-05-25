package CoroUtil.difficulty.data.conditions;

import CoroUtil.difficulty.data.DataCondition;

/**
 * Created by Corosus on 2/26/2017.
 */
public class ConditionInvasionRate extends DataCondition {
    public int rate;

    @Override
    public String toString() {
        return "wave rate: " + rate;
    }
}
