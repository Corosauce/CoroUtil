package CoroUtil.difficulty.data.cmods;

import CoroUtil.difficulty.data.DataCmod;
import CoroUtil.difficulty.data.DataCmodTemplate;
import CoroUtil.difficulty.data.DifficultyDataReader;
import com.mojang.realmsclient.gui.ChatFormatting;

/**
 * Created by Corosus on 2/26/2017.
 */
public class CmodTemplateReference extends DataCmod {
    public String template;

    @Override
    public String toString() {
        String code = "";
        if (DifficultyDataReader.debugValidate()) {
            code = ChatFormatting.GREEN.toString();
            if (!DifficultyDataReader.getData().lookupCmodTemplates.containsKey(template)) {
                code = ChatFormatting.RED.toString() + "MISSING! ";
            }
        }

        return super.toString() + " { " + code + template + " }";
    }
}
