package CoroAI.util;

import net.minecraft.item.Item;

public class CoroUtilItem {

	public static Item setUnlocalizedNameAndTexture(Item item, String nameTex) {
		item.setUnlocalizedName(nameTex);
		item.func_111206_d(nameTex);
    	return item;
    }
	
}
