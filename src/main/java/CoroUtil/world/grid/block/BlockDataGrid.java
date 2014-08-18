package CoroUtil.world.grid.block;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilFile;


public class BlockDataGrid
{
	
	//this grid purposely trims out air blocks where it can
	
	public World world;
    public HashMap<Integer, BlockDataPoint> grid;

    public BlockDataGrid(World parWorld)
    {
    	world = parWorld;
        grid = new HashMap();
    }

    public int getHash(int i, int j, int k)
    {
        return j & 0xff | (i & 0x7fff) << 8 | (k & 0x7fff) << 24 | (i >= 0 ? 0 : 0x80000000) | (k >= 0 ? 0 : 0x8000);
    }
    
    public float getBlockStrength(int x, int y, int z) {
    	Block blockID = world.getBlock(x, y, z);
    	int blockMeta = world.getBlockMetadata(x, y, z);
    	
    	//use block id to weight lookup map
    	
    	return BlockStaticDataMap.getBlockStength(blockID);
    }
    
    public BlockDataPoint getBlockData(int i, int j, int k)
    {
    	return getBlockData(i, j, k, false, false);
    }
    
    public BlockDataPoint getBlockDataIfExists(int i, int j, int k)
    {
    	return getBlockData(i, j, k, false, true);
    }

    //returns null if air block unless told to not check
    public BlockDataPoint getBlockData(int i, int j, int k, boolean skipAirCheckOnCreate, boolean onlyIfExists)
    {
    	
    	//i could technically add a check to see if bdp.isRemovable(), but thats bad practice, lets plan to have our system keep it clean, and let it balloon up so we can see the issue instead of hiding the issue
    	
        int hash = getHash(i, j, k);

        if (!grid.containsKey(hash))
        {
        	if (!onlyIfExists) {
	        	if (skipAirCheckOnCreate || !CoroUtilBlock.isAir(this.world.getBlock(i, j, k))) {
	        		BlockDataPoint newVec = new BlockDataPoint(this, i, j, k);
	                grid.put(newVec.hash, newVec);
	                return newVec;
	        	} else {
	        		System.out.println("Epoch BlockDataGrid detected air block load, skipping");
	        		return null;
	        	}
        	} else {
        		return null;
        	}
        }
        else
        {
            return grid.get(hash);
        }
    }
    
    public void removeBlockData(int i, int j, int k) {
    	int hash = getHash(i, j, k);

        if (grid.containsKey(hash))
        {
        	//perhaps theres a better memory managed way to clean these objects up?
        	BlockDataPoint bdp = grid.get(hash);
        	bdp.cleanup();
        	grid.remove(hash);
        	
        	//System.out.println("grid had removal, new size: " + grid.size());
        }
    }
    
    public void readFromFile() {
		try {
			
			String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "epoch" + File.separator;
			
			if ((new File(saveFolder + "EpochBlockDataDim_" + world.provider.dimensionId + ".dat")).exists()) {
				NBTTagCompound data = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "EpochBlockDataDim_" + world.provider.dimensionId + ".dat"));
				
				//Collection playerDataCl = data.getTags();
				Iterator it = data.func_150296_c().iterator();//playerDataCl.iterator();
				
				while (it.hasNext()) {
					String keyName = (String)it.next();
					NBTTagCompound nbt = data.getCompoundTag(keyName);
					
					BlockDataPoint bdp = this.getBlockData(nbt.getInteger("xCoord"), nbt.getInteger("yCoord"), nbt.getInteger("zCoord"));
					if (bdp != null) {
						bdp.readFromNBT(nbt);
					} else {
						//must have been set to air at some point...
					}
					
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void writeToFile(boolean unloadInstances) {
    	try {
    		
    		NBTTagCompound data = new NBTTagCompound();
    		
    		Collection playerDataCl = grid.values();
			Iterator it = playerDataCl.iterator();
			
			while (it.hasNext()) {
				BlockDataPoint bdp = (BlockDataPoint)it.next();
				data.setTag(""+bdp.hash, bdp.writeToNBT());
			}
    		
    		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "epoch" + File.separator;
    		
    		//Write out to file
    		if (!(new File(saveFolder).exists())) (new File(saveFolder)).mkdirs();
    		FileOutputStream fos = new FileOutputStream(saveFolder + "EpochBlockDataDim_" + world.provider.dimensionId + ".dat");
	    	CompressedStreamTools.writeCompressed(data, fos);
	    	fos.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

    public void tick()
    {
    	
    }
}
