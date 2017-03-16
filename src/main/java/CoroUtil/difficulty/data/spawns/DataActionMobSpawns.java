package CoroUtil.difficulty.data.spawns;

import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.difficulty.data.DataCmod;
import CoroUtil.difficulty.data.DeserializerAllJson;
import CoroUtil.difficulty.data.DifficultyDataReader;
import CoroUtil.util.CoroUtilEntity;
import com.mojang.realmsclient.gui.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Corosus on 2/26/2017.
 */
public class DataActionMobSpawns {

    public int count;
    public List<String> entities = new ArrayList<>();
    public List<DataCmod> cmods = new ArrayList<>();

    @Override
    public String toString() {
        String str = ChatFormatting.GOLD + "Entities: " + ChatFormatting.RESET;
        for (String entity : entities) {
            String code = "";
            if (DifficultyDataReader.debugValidate()) {
                code = ChatFormatting.GREEN.toString();
                if (CoroUtilEntity.getClassFromRegisty(entity) == null)
                    code = ChatFormatting.RED.toString() + "MISSING! ";
            }
            str += code + entity + ", ";
        }
        str += " | " + ChatFormatting.GOLD + "With cmods: " + ChatFormatting.RESET;
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
