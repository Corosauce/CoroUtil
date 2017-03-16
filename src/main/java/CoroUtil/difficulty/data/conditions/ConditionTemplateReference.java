package CoroUtil.difficulty.data.conditions;

import CoroUtil.difficulty.data.DataCondition;
import CoroUtil.difficulty.data.DifficultyDataReader;
import com.mojang.realmsclient.gui.ChatFormatting;

/**
 * Created by Corosus on 2/26/2017.
 */
public class ConditionTemplateReference extends DataCondition {
    public String template;

    @Override
    public String toString() {
        String code = "";
        if (DifficultyDataReader.debugValidate()) {
            code = ChatFormatting.GREEN.toString();
            if (!DifficultyDataReader.getData().lookupConditionTemplates.containsKey(template)) {
                code = ChatFormatting.RED.toString() + "MISSING! ";
            }
        }

        return super.toString() + " { " + code + template + " }";
    }
}
