package CoroUtil.difficulty.data.spawns;

import CoroUtil.difficulty.data.DataCmod;
import CoroUtil.difficulty.data.DeserializerAllJson;
import CoroUtil.difficulty.data.DifficultyDataReader;
import CoroUtil.util.CoroUtilEntity;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Corosus on 2/26/2017.
 */
public class DataActionMobSpawns {

    public int count;
    public int count_max = -1;
    public double count_difficulty_multiplier = 0;

    public List<String> entities = new ArrayList<>();
    public List<DataCmod> cmods = new ArrayList<>();

    public DataActionMobSpawns copy() {
        DataActionMobSpawns copy = new DataActionMobSpawns();
        //probably pointless, should always be 0 unless something messed with original copy
        copy.count = count;
        copy.count_max = count_max;
        copy.count_difficulty_multiplier = count_difficulty_multiplier;
        copy.entities.addAll(entities);
        for (DataCmod cmod : cmods) {
            copy.cmods.add(cmod);
        }
        return copy;
    }

    public int getMaxMobCountDynamic(double difficulty) {
        int count = this.count;
        count += count * (difficulty * count_difficulty_multiplier);
        if (count_max != -1 && count > count_max) {
            count = count_max;
        }
        return count;
    }

    @Override
    public String toString() {
        String str = TextFormatting.GOLD + "Entities: " + TextFormatting.RESET;
        for (String entity : entities) {
            String code = "";
            if (DifficultyDataReader.debugValidate()) {
                code = TextFormatting.GREEN.toString();
                if (CoroUtilEntity.getClassFromRegistry(entity) == null)
                    code = TextFormatting.RED.toString() + "MISSING! ";
            }
            str += code + entity + ", ";
        }

        str += "a count of " + count + " * difficulty * multiplier of " + count_difficulty_multiplier + " maxing at " + count_max;
        if (DifficultyDataReader.getDebugDifficulty() != -1) {
            str += ", test calculated to count of " + getMaxMobCountDynamic(DifficultyDataReader.getDebugDifficulty());
        }
        str += " | " + TextFormatting.GOLD + "With cmods: " + TextFormatting.RESET;
        List<DataCmod> cmodsToUse = cmods;
        if (DifficultyDataReader.debugFlattenCmodsAndConditions()) {
            cmodsToUse = DeserializerAllJson.getCmodsFlattened(cmods);
        }
        for (DataCmod cmod : cmodsToUse) {
            str += cmod.toString() + ", ";
        }
        str += " | ";
        return str;
    }
}
