package CoroUtil.util;

import net.minecraft.item.Item;

public class CoroUtilItem {

	public static Item setUnlocalizedNameAndTexture(Item item, String nameTex) {
		item.setUnlocalizedName(nameTex);
		item.setTextureName(nameTex);
    	return item;
    }
	
	public static Item getItemByName(String name) {
		try {
			return (Item) Item.itemRegistry.getObject(name);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static String getNameByItem(Item item) {
		return Item.itemRegistry.getNameForObject(item);
	}
	
	
	
}
