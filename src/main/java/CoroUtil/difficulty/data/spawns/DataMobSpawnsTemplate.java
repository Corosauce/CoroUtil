package CoroUtil.difficulty.data.spawns;

import CoroUtil.difficulty.data.DataCondition;
import CoroUtil.difficulty.data.spawns.DataActionMobSpawns;
import com.mojang.realmsclient.gui.ChatFormatting;

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
        String str = ChatFormatting.AQUA + "Profile: " + ChatFormatting.RESET + name + " | ";
        for (DataActionMobSpawns spawn : spawns) {
            str += spawn.toString() + " | ";
        }
        return str;
    }
}
