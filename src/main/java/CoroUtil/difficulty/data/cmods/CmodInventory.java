package CoroUtil.difficulty.data.cmods;

import CoroUtil.difficulty.data.DataCmod;
import CoroUtil.difficulty.data.DifficultyDataReader;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;

/**
 * Created by Corosus on 2/26/2017.
 */
public class CmodInventory extends DataCmod {
    public String inv_hand_main;
    public String inv_hand_off;
    public String inv_head;
    public String inv_chest;
    public String inv_legs;
    public String inv_feet;

    @Override
    public String toString() {
        String srcString = super.toString() + ", items: ";
        srcString += getValidatedString(inv_hand_main);
        srcString += getValidatedString(inv_hand_off);
        srcString += getValidatedString(inv_head);
        srcString += getValidatedString(inv_chest);
        srcString += getValidatedString(inv_legs);
        srcString += getValidatedString(inv_feet);

        return srcString;
    }

    public String getValidatedString(String registryName) {
        String code = "";
        if (DifficultyDataReader.debugValidate() && registryName != null && !registryName.equals("")) {
            code = TextFormatting.GREEN.toString();
            Item item = null;
            try {
                item = Item.getByNameOrId(registryName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (item == null) {
                code = TextFormatting.RED.toString() + "MISSING! ";
            }
        }
        if (registryName == null || registryName.equals("")) {
            registryName = "blank";
        }
        return code + registryName + TextFormatting.RESET + ", ";
    }
}
