package CoroUtil.difficulty.data.conditions;

import CoroUtil.difficulty.data.DataCondition;
import CoroUtil.difficulty.data.DifficultyDataReader;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;

/**
 * Created by Corosus on 2/26/2017.
 */
public class ConditionModLoaded extends DataCondition {

    public String mod_id;

    @Override
    public String toString() {
        String code = "";
        if (DifficultyDataReader.debugValidate()) {
            code = TextFormatting.GREEN.toString();
            if (!Loader.isModLoaded(mod_id)) {
                code = TextFormatting.RED.toString() + "MOD MISSING! ";
            }
        }

        return super.toString() + " { " + code + mod_id + " } ";
    }
}
