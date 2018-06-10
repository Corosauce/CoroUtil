package CoroUtil.difficulty.data.spawns;

import CoroUtil.difficulty.data.DataCondition;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Corosus on 2/26/2017.
 */
public class DataMobSpawnsTemplate {

    public String name;
    public List<DataCondition> conditions = new ArrayList<>();
    public List<DataActionMobSpawns> spawns = new ArrayList<>();

    public List<DataCondition> getConditions() {
        return conditions;
    }



    @Override
    public String toString() {
        String str = TextFormatting.AQUA + "Profile: " + TextFormatting.RESET + name + " | ";
        for (DataActionMobSpawns spawn : spawns) {
            str += spawn.toString() + " | ";
        }
        str += TextFormatting.GOLD + "For conditions: " + TextFormatting.RESET;
        for (DataCondition cond : conditions) {
            str += cond.toString() + " | ";
        }
        return str;
    }
}
