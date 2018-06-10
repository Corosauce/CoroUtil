package CoroUtil.difficulty.data.conditions;

import CoroUtil.difficulty.data.DataCondition;

/**
 * Created by Corosus on 2/26/2017.
 */
public class ConditionDifficulty extends DataCondition {
    public double min;
    public double max;

    @Override
    public String toString() {
        return super.toString() + " { " + "range: " + min + " to " + max + " }";
    }
}
