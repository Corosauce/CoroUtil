package CoroUtil.difficulty.data.cmods;

import CoroUtil.difficulty.data.*;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * Created by Corosus on 2/26/2017.
 */
public class CmodTemplateReference extends DataCmod {
    public String template;

    @Override
    public String toString() {
        String code = "";
        if (DifficultyDataReader.debugValidate()) {
            code = TextFormatting.GREEN.toString();
            if (!DifficultyDataReader.getData().lookupCmodTemplates.containsKey(template)) {
                code = TextFormatting.RED.toString() + "MISSING! ";
            }
        }

        String flattened = "";
        List<DataCmod> list = DeserializerAllJson.getCmodFlattened(this);
        for (DataCmod cond : list) {
            flattened += cond.toString() + ", ";
        }
        if (flattened.length() > 0) flattened = flattened.substring(0, flattened.length() - 2);

        return super.toString() + " { " + code + template + " } [ flat: " + flattened + " ] ";
    }
}

