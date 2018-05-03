package CoroUtil.difficulty.data.conditions;

import CoroUtil.difficulty.data.DataCondition;
import CoroUtil.difficulty.data.DeserializerAllJson;
import CoroUtil.difficulty.data.DifficultyDataReader;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * Created by Corosus on 2/26/2017.
 */
public class ConditionTemplateReference extends DataCondition {
    public String template;

    @Override
    public String toString() {
        String code = "";
        if (DifficultyDataReader.debugValidate()) {
            code = TextFormatting.GREEN.toString();
            if (!DifficultyDataReader.getData().lookupConditionTemplates.containsKey(template)) {
                code = TextFormatting.RED.toString() + "MISSING! ";
            }
        }

        String flattened = "";
        List<DataCondition> list = DeserializerAllJson.getConditionFlattened(this);
        for (DataCondition cond : list) {
            flattened += cond.toString() + ", ";
        }
        if (flattened.length() > 0) flattened = flattened.substring(0, flattened.length() - 2);

        return super.toString() + " { " + code + template + TextFormatting.RESET + " } [ flat: " + flattened + " ] ";
    }
}
