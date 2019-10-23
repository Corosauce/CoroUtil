package CoroUtil.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

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
			return (Block) Block.REGISTRY.getOrDefault(new ResourceLocation(name));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/*public static String getNameByItem(Item item) {
		return Block.blockRegistry.getKey(item);
	}*/
	
	public static String getNameByBlock(Block item) {
		return Block.REGISTRY.getKey(item).toString();
	}
	
}
