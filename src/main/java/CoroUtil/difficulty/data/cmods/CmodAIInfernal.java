package CoroUtil.difficulty.data.cmods;

import CoroUtil.difficulty.data.DataCmod;
import CoroUtil.difficulty.data.DifficultyDataReader;
import CoroUtil.util.CoroUtilCrossMod;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * Created by Corosus on 2/26/2017.
 */
public class CmodAIInfernal extends DataCmod {
    public List<String> modifiers;

    public int randomly_choose_count;
    public int randomly_choose_count_max = -1;
    public double difficulty_multiplier;

    @Override
    public String toString() {
        String mods = "";
        for (String entry : modifiers) {
            String code = "";
            if (DifficultyDataReader.debugValidate()) {
                code = TextFormatting.GREEN.toString();
                if (!CoroUtilCrossMod.listModifiers.contains(entry)) {
                    code = TextFormatting.RED.toString() + "MISSING! ";
                }
            }
            mods += code + entry + ", ";
        }
        return super.toString() + " randomly_choose_count: " + randomly_choose_count + " randomly_choose_count_max: " + randomly_choose_count_max + ", difficulty_multiplier: " + difficulty_multiplier + ", mods: " + mods;
    }
}
