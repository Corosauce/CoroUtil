package CoroUtil.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class CoroUtilBlock {

	/*public static Block setUnlocalizedNameAndTexture(Block block, String nameTex) {
		block.setBlockName(nameTex);
		//block.setTextureName(nameTex);
    	return block;
    }*/
	
	public static boolean isAir(Block parBlock) {
		Material mat = parBlock.getDefaultState().getMaterial();
		if (mat == Material.AIR) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isEqual(Block parBlock, Block parBlock2) {
		return parBlock == parBlock2;
	}
	
	public static boolean isEqualMaterial(Block parBlock, Material parMaterial) {
		return parBlock.getMaterial(parBlock.getDefaultState()) == parMaterial;
	}
	
	public static Block getBlockByName(String name) {
		try {
			return (Block) Block.REGISTRY.getObject(new ResourceLocation(name));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/*public static String getNameByItem(Item item) {
		return Block.blockRegistry.getNameForObject(item);
	}*/
	
	public static String getNameByBlock(Block item) {
		return Block.REGISTRY.getNameForObject(item).toString();
	}
	
}
