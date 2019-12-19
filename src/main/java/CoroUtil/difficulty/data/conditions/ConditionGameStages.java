package CoroUtil.difficulty.data.conditions;

import CoroUtil.difficulty.data.DataCondition;
import CoroUtil.difficulty.data.DifficultyDataReader;
import CoroUtil.util.CoroUtilCrossMod;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;

import java.util.List;

/**
 * Created by Corosus on 2/26/2017.
 */
public class ConditionGameStages extends DataCondition {

    public List<String> game_stages;
    public String match_mode = "any_of";

    @Override
    public String toString() {
        String mods = "";
        for (String entry : game_stages) {
            String code = "";
            mods += code + entry + ", ";
        }
        return super.toString() + " { " + "game_stages: [ " + mods + " ], match_mode: " + match_mode + " } ";
    }

    public boolean matchAll() {
        return match_mode.equals("all_of");
    }

    public boolean matchAny() {
        return match_mode.equals("any_of");
    }
}
