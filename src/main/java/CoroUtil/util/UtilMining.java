package CoroUtil.util;

import CoroUtil.forge.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class UtilMining {

    public static boolean canMineBlock(World world, BlockCoord pos, Block block) {
    	
    	//System.out.println("check: " + block);
    	
    	IBlockState state = world.getBlockState(pos.toBlockPos());


		/**TODO: check BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, entityPlayer); if is
		 * event.setCanceled(preCancelEvent);
		 * MinecraftForge.EVENT_BUS.post(event);
		 * needs fakeplayer
		 */
    	
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

    public static boolean canConvertToRepairingBlock(World world, IBlockState state) {
		//should cover most all types we dont want to put into repairing state
		if (!state.isFullCube()) {
			return false;
		}
		return true;
	}
	
}
