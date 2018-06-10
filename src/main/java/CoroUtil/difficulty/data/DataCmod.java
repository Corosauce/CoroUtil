package CoroUtil.difficulty.data;

import net.minecraft.util.text.TextFormatting;

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
        return TextFormatting.GREEN + cmod + TextFormatting.RESET;
    }
}
