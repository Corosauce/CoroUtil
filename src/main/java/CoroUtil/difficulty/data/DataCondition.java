package CoroUtil.difficulty.data;

import net.minecraft.util.text.TextFormatting;

/**
 * Created by Corosus on 2/26/2017.
 */
public class DataCondition {
    public String condition;

    @Override
    public String toString() {
        return TextFormatting.GREEN + condition + TextFormatting.RESET;
    }
}
