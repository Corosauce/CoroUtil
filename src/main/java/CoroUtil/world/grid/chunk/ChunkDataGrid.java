package CoroUtil.world.grid.chunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import CoroUtil.util.CoroUtilFile;


public class ChunkDataGrid
{
	
	//this grid purposely trims out air blocks where it can
	
	public World world;
    public HashMap<Integer, ChunkDataPoint> grid;

    public ChunkDataGrid(World parWorld)
    {
    	world = parWorld;
        grid = new HashMap();
    }

    public int getHash(int i, int k)
    {
    	int j = 0; //no y
        return j & 0xff | (i & 0x7fff) << 8 | (k & 0x7fff) << 24 | (i >= 0 ? 0 : 0x80000000) | (k >= 0 ? 0 : 0x8000);
    }
    
    public ChunkDataPoint getChunkData(int i, int k)
    {
    	return getChunkData(i, k, false);
    }
    
    public ChunkDataPoint getBlockDataIfExists(int i, int k)
    {
    	return getChunkData(i, k, true);
    }

    //returns null if air block unless told to not check
    public ChunkDataPoint getChunkData(int i, int k, boolean onlyIfExists)
    {
    	
    	//i could technically add a check to see if bdp.isRemovable(), but thats bad practice, lets plan to have our system keep it clean, and let it balloon up so we can see the issue instead of hiding the issue
    	
        int hash = getHash(i, k);

        if (!grid.containsKey(hash))
        {
        	if (!onlyIfExists) {
        		ChunkDataPoint newVec = new ChunkDataPoint(this, i, k);
        		newVec.initFirstTime();
                grid.put(newVec.hash, newVec);
                return newVec;
	        } else {
        		return null;
        	}
        }
        else
        {
            return grid.get(hash);
        }
    }
    
    public void removeBlockData(int i, int k) {
    	int hash = getHash(i, k);

        if (grid.containsKey(hash))
        {
        	//perhaps theres a better memory managed way to clean these objects up?
        	ChunkDataPoint bdp = grid.get(hash);
        	bdp.cleanup();
        	grid.remove(hash);
        	
        	//System.out.println("grid had removal, new size: " + grid.size());
        }
    }
    
    public void readFromFile() {
		try {
			
			String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "CoroUtil" + File.separator + "World" + File.separator;
			
			if ((new File(saveFolder + "ChunkDataDim_" + world.provider.getDimension() + ".dat")).exists()) {
				NBTTagCompound data = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "ChunkDataDim_" + world.provider.getDimension() + ".dat"));
				
				/*Collection playerDataCl = data.getTags();
				Iterator it = playerDataCl.iterator();*/
				
				Iterator it = data.getKeySet().iterator();
				
				while (it.hasNext()) {
					String keyName = (String)it.next();
					NBTTagCompound nbt = data.getCompoundTag(keyName);
					
					ChunkDataPoint bdp = this.getChunkData(nbt.getInteger("xCoord"), nbt.getInteger("zCoord"));
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
				ChunkDataPoint bdp = (ChunkDataPoint)it.next();
				data.setTag(""+bdp.hash, bdp.writeToNBT());
			}
    		
    		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "CoroUtil" + File.separator + "World" + File.separator;
    		
    		//Write out to file
    		if (!(new File(saveFolder).exists())) (new File(saveFolder)).mkdirs();
    		FileOutputStream fos = new FileOutputStream(saveFolder + "ChunkDataDim_" + world.provider.getDimension() + ".dat");
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
