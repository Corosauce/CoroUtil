package CoroUtil.difficulty.data.cmods;

import CoroUtil.difficulty.data.DataCmod;
import CoroUtil.difficulty.data.DifficultyDataReader;
import CoroUtil.util.CoroUtilCrossMod;
import com.mojang.realmsclient.gui.ChatFormatting;

import java.util.List;

/**
 * Created by Corosus on 2/26/2017.
 */
public class CmodAIInfernal extends DataCmod {
    public List<String> modifiers;

    @Override
    public String toString() {
        String mods = "";
        for (String entry : modifiers) {
            String code = "";
            if (DifficultyDataReader.debugValidate()) {
                code = ChatFormatting.GREEN.toString();
                if (!CoroUtilCrossMod.listModifiers.contains(entry)) {
                    code = ChatFormatting.RED.toString() + "MISSING! ";
                }
            }
            mods += code + entry + ", ";
        }
        return super.toString() + ", mods: " + mods;
    }
}
