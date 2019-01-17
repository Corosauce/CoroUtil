package CoroUtil.util;

import CoroUtil.forge.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UtilMining {

	public static boolean canMineBlock(World world, BlockCoord pos, Block block) {
		return canMineBlock(world, pos.toBlockPos(), block);
	}

    public static boolean canMineBlock(World world, BlockPos pos, Block block) {
    	
    	//System.out.println("check: " + block);
    	
    	IBlockState state = world.getBlockState(pos);


		/**TODO: check BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, entityPlayer); if is
		 * event.setCanceled(preCancelEvent);
		 * MinecraftForge.EVENT_BUS.post(event);
		 * needs fakeplayer
		 */
    	
    	//dont mine tile entities
		if (block.isAir(state, world, pos) || block == CommonProxy.blockRepairingBlock) {
			return false;
		}
    	if (world.getTileEntity(pos) != null) {
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

		if (state.getMaterial() == Material.GLASS) {
			return true;
		}

		//should cover most all types we dont want to put into repairing state
		if (!state.isFullCube()) {
			return false;
		}
		return true;
	}
	
}
