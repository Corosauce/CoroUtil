package CoroUtil.difficulty.data;

import com.mojang.realmsclient.gui.ChatFormatting;

/**
 * Created by Corosus on 2/26/2017.
 */
public class DataCmod {
    public String cmod;

    public DataCmod copy() {
        DataCmod copy = new DataCmod();
        copy.cmod = cmod;
        return copy;
    }

    @Override
    public String toString() {
        return ChatFormatting.GREEN + cmod + ChatFormatting.RESET;
    }
}
