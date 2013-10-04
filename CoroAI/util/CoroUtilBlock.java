package CoroAI.util;

import net.minecraft.block.Block;

public class CoroUtilBlock {

	public static Block setUnlocalizedNameAndTexture(Block block, String nameTex) {
		block.setUnlocalizedName(nameTex);
		block.setTextureName(nameTex);
    	return block;
    }
	
}
