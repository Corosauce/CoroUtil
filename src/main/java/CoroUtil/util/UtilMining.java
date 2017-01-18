package CoroUtil.util;

import CoroUtil.forge.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import CoroUtil.util.BlockCoord;

public class UtilMining {

    public static boolean canMineBlock(World world, BlockCoord pos, Block block) {
    	
    	//System.out.println("check: " + block);
    	
    	IBlockState state = world.getBlockState(pos.toBlockPos());
    	
    	//dont mine tile entities
    	if (world.getTileEntity(pos.toBlockPos()) != null) {
    		return false;
    	}
    	if (block.isAir(state, world, pos.toBlockPos()) || block == CommonProxy.blockRepairingBlock) {
    		return false;
    	}
    	/*if (block == Blocks.obsidian) {
    		return false;
    	}*/
    	if (state.getMaterial().isLiquid()) {
    		return false;
    	}
    	
    	return true;
    }
	
}
