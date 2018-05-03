package CoroUtil.difficulty.data.conditions;

import CoroUtil.difficulty.data.DataCondition;

/**
 * Created by Corosus on 2/26/2017.
 */
public class ConditionContext extends DataCondition {

    public static String TYPE_INVASION = "invasion";
    public static String TYPE_REGULAR = "regular";
    public static String TYPE_ALL = "all";

    public String type;

    @Override
    public String toString() {
        return super.toString() + " { " + type + " } ";
    }
}
