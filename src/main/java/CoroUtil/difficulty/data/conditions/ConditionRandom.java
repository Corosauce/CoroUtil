package CoroUtil.difficulty.data.conditions;

import CoroUtil.difficulty.data.DataCondition;

/**
 * Created by Corosus on 2/26/2017.
 */
public class ConditionRandom extends DataCondition {
    public int weight;

    @Override
    public String toString() {
        return super.toString() + " { " + "weight: " + weight + " }";
    }
}
