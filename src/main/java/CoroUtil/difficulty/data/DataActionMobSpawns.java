package CoroUtil.difficulty.data;

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
            str += entity + ", ";
        }
        str += " | " + ChatFormatting.GOLD + "With cmods: " + ChatFormatting.RESET;
        for (DataCmod cmod : cmods) {
            str += cmod.toString() + ", ";
        }
        str += " | ";
        return str;
    }
}
