package CoroUtil.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

public class CoroUtilBlock {

	public static Block setUnlocalizedNameAndTexture(Block block, String nameTex) {
		block.setBlockName(nameTex);
		//block.setTextureName(nameTex);
    	return block;
    }
	
	public static boolean isAir(Block parBlock) {
		Material mat = parBlock.getMaterial();
		if (mat == Material.air) {
			return true;
		} else {
			return false;
		}
	}
	
	public static Block getBlockByName(String name) {
		try {
			return (Block) Block.blockRegistry.getObject(name);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static String getNameByItem(Item item) {
		return Block.blockRegistry.getNameForObject(item);
	}
	
}
