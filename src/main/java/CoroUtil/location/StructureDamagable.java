package CoroUtil.location;

import java.util.List;

import CoroUtil.util.ChunkCoordinatesBlock;
import CoroUtil.util.CoroUtilBlock;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class StructureDamagable extends StructureObject {
	
	public int healthCur;
	public int healthMax;

	public StructureDamagable() {
		
	}
	
	@Override
	public void tickUpdate() {
		super.tickUpdate();
		
		boolean needCacheUpdate = false;
		World world = DimensionManager.getWorld(location.dimID);
		
		if (world != null) {
			//temp
			if (world.getTotalWorldTime() % (20*300) == 0) {
				//needCacheUpdate = true;
			}
			
			if (needCacheUpdate) {
				//System.out.println("Updating building cache - thread me!");
				List<ChunkCoordinatesBlock> data = getStructureGenerationComplete(false);
				healthCur = getStructureHealth(data, false);
				
				if (/*true || */healthCur < healthMax / 4 * 3) {
					//System.out.println("rebuilding!");
					System.out.println(name + " rebuilding, health: " + healthCur + " / " + healthMax);
					buildStructureComplete(false);
				} else {
					//System.out.println(name + " health: " + healthCur + " / " + healthMax);
				}
			}
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound var1) {
		super.readFromNBT(var1);
        healthCur = var1.getInteger("healthCur");
        healthMax = var1.getInteger("healthMax");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound var1) {
		super.writeToNBT(var1);
        var1.setInteger("healthCur", healthCur);
        var1.setInteger("healthMax", healthMax);
	}
	
	@Override
	public void updateStructureState(List<ChunkCoordinatesBlock> parStructure, boolean firstTimeGen) {
		super.updateStructureState(parStructure, firstTimeGen);
		
		healthCur = getStructureHealth(parStructure, firstTimeGen);
	}
	
	public int getStructureHealth(List<ChunkCoordinatesBlock> parStructure, boolean andUpdateMaxHealth) {
		float totalHealthCur = 0;
		float totalHealthMax = 0; //only accurate when this is run on a fully untouched building (no block removals)
		World world = DimensionManager.getWorld(location.dimID);
		int blockCount = 0;
		if (world != null) {
			for (int i = 0; i < parStructure.size(); i++) {
				ChunkCoordinatesBlock coords = parStructure.get(i);
				int x = coords.posX;
				int y = coords.posY;
				int z = coords.posZ;
				
				Block id = world.getBlock(x, y, z);
				//System.out.println("coords: " + x + " - " + y + " - " + z + " = " + id);
				if (!CoroUtilBlock.isAir(id)) {
					blockCount++;
					System.out.println("TODO: BlockDataGrid usage");
					/*BlockDataPoint bdp = ServerTickHandler.wd.getBlockDataGrid(world).getBlockDataIfExists(x, y, z);
					float maxHealth = BlockStaticDataMap.getBlockMaxHealth(id);
					if (bdp == null) {
						totalHealthCur += maxHealth;
					} else {
						totalHealthCur += bdp.health;
						//System.out.println("bdp.health " + bdp.health);
					}
					totalHealthMax += maxHealth;*/
				}
				
			}
		}
		if (andUpdateMaxHealth) {
			healthMax = (int)totalHealthMax;
		}
		//System.out.println("blockCount: " + blockCount);
		
		return (int)totalHealthCur;
	}
	
	@Override
	public void buildPattern(List<ChunkCoordinatesBlock> parStructure) {
		//int blockID = Block.cobblestone.blockID;
		World world = DimensionManager.getWorld(location.dimID);
		if (world != null) {
			for (int i = 0; i < parStructure.size(); i++) {
				ChunkCoordinatesBlock coords = parStructure.get(i);
				//System.out.println(coords.posX + " - " + coords.posY + " - " + coords.posZ + " - " + world.provider.dimensionId);
				System.out.println("TODO: BlockDataGrid usage");
				/*BlockDataPoint bdp = ServerTickHandler.wd.getBlockDataGrid(world).getBlockDataIfExists(coords.posX, coords.posY, coords.posZ);
				//if (coords.blockID == 0) System.out.println("sadfsdfsdf");
				//NOTE, THIS WONT PRINT OVER DAMAGED BUT STILL EXISTING BLOCKS!
				//if ((bdp != null && bdp.health < BlockStaticDataMap.getBlockMaxHealth(bdp.blockID)) || (bdp == null && isSafeToGenerateOver(coords.blockID))) {
					world.setBlock(coords.posX, coords.posY, coords.posZ, coords.block, coords.meta, 3);
					if (bdp != null) {
						bdp.health = BlockStaticDataMap.getBlockMaxHealth(coords.block);
					}
				//}
*/			}
		}
	}
	
}
