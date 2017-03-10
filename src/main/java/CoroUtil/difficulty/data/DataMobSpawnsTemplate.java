package CoroUtil.difficulty.data;

import CoroUtil.difficulty.data.conditions.ConditionTemplate;
import com.mojang.realmsclient.gui.ChatFormatting;

import java.util.ArrayList;
import java.util.HashMap;
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

    /**
     * Get conditions including ones nested in templates
     * - aiming to use overridable conditions
     * - top level condition overrides one in a template if are of the same type
     * - there may be scenarios where i want to layer and use both conditions?
     * - what about 2 templates referenced, each with a random?
     * @return
     */
    public List<DataCondition> getConditionsFlattened() {
        //List<DataCondition> listTop = new ArrayList<>();
        HashMap<String, DataCondition> lookup = new HashMap<>();
        //conditions.contains()

        for (DataCondition condition : conditions) {
            if (condition instanceof ConditionTemplate) {

            } else {
                if (!lookup.containsKey(condition.condition)) {
                    lookup.put(condition.condition, condition);
                } else {
                    CoroUtil.forge.CoroUtil.dbg("duplicate key for condition at top level " + condition.condition);
                }
            }

        }

        for (DataCondition condition : conditions) {
            if (condition instanceof ConditionTemplate) {
                DataConditionTemplate template = DifficultyDataReader.getData().lookupConditionTemplates.get(((ConditionTemplate) condition).template);
                if (template != null) {
                    for (DataCondition condition2 : template.conditions) {
                        //prevent nested templating for now
                        if (!(condition2 instanceof ConditionTemplate)) {
                            if (!lookup.containsKey(condition2.condition)) {
                                lookup.put(condition2.condition, condition2);
                            } else {
                                CoroUtil.forge.CoroUtil.dbg("duplicate key for condition from template " + condition2.condition);
                            }
                        }
                    }
                }
            }
        }

        List<DataCondition> list = new ArrayList<>();
        list.addAll(lookup.values());
        return list;
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
