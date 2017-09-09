package CoroUtil.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CoroUtilInventory {

	public static int getItemCount(IInventory inv, String id) {
		int count = 0;
		for(int j = 0; j < inv.getSizeInventory(); j++)
        {
			ItemStack is = inv.getStackInSlot(j);
            if(is != null && is.getItem() != null)
            {
            	String itemName = CoroUtilItem.getNameByItem(is.getItem());
            	if (itemName != null) {
            		if (itemName.equals(id)) {
            			count += is.getCount();
            		}
            	} else {
            		System.out.println("CoroUtilInventory.getItemCount found nameless item " + is.getItem() + " in inventory, its not registered properly!");
            	}
            }
        }
		
		return count;
	}
	
	public static boolean isChest(Block block) {
		if (block instanceof BlockChest) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void chestOpen(World world, int x, int y, int z) {
		//System.out.println("new ai chest open called, needs tracking for closing chest");
		chestStateSend(world, x, y, z, false);
	}
	
	public static void chestClose(World world, int x, int y, int z) {
		chestStateSend(world, x, y, z, true);
	}
	
	//TODO: 1.8 redesign to pass along entity reference
	public static void chestStateSend(World world, int x, int y, int z, boolean close) {
		/*if (isChest(world.getBlockState(new BlockPos(x, y, z)).getBlock())) {
			TileEntity chest = (TileEntity)world.getTileEntity(new BlockPos(x, y, z));
			if (chest instanceof TileEntityChest) {
				if (close) {
					((TileEntityChest)chest).closeInventory();
				} else {
					((TileEntityChest)chest).openInventory();
				}
			}
		}*/
	}
	
	/*public static boolean chestTryTransfer(World world, ICoroAI ai, int x, int y, int z) {
    	
    	TileEntity tEnt = (TileEntityChest)world.getTileEntity(new BlockPos(x, y, z));
		if (tEnt instanceof TileEntityChest) {
			TileEntityChest chest = (TileEntityChest)tEnt;
			chestOpen(world, x, y, z);
			ai.getAIAgent().jobMan.getPrimaryJob().transferItems(ai.getAIAgent().entInv.inventory, chest, "-1", -1, true);
			return true;
		}
		return false;
    }*/
	
}
