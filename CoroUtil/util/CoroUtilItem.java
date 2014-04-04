package CoroUtil.util;

import net.minecraft.item.Item;

public class CoroUtilItem {

	public static Item setUnlocalizedNameAndTexture(Item item, String nameTex) {
		item.setUnlocalizedName(nameTex);
		item.setTextureName(nameTex);
    	return item;
    }
	
}
