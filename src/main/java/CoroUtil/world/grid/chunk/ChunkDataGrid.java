package CoroUtil.world.grid.chunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import CoroUtil.forge.CULog;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import CoroUtil.util.CoroUtilFile;


public class ChunkDataGrid
{
	
	//this grid purposely trims out air blocks where it can
	
	public World world;
    public HashMap<Integer, ChunkDataPoint> grid;

    //runtime nbt data, required for injecting partial datasets into nbt to write out
	//solves long term servers getting overloaded with chunk data
    public NBTTagCompound dataAllChunks = new NBTTagCompound();

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
        		//NEW, try load from persistent nbt first
				if (dataAllChunks.hasKey(""+hash)) {
					NBTTagCompound nbt = dataAllChunks.getCompoundTag(""+hash);

					//ChunkDataPoint bdp = this.getChunkData(nbt.getInteger("xCoord"), nbt.getInteger("zCoord"));
					ChunkDataPoint bdp = new ChunkDataPoint(this, nbt.getInteger("xCoord"), nbt.getInteger("zCoord"));
					bdp.initFirstTime();
					bdp.readFromNBT(nbt);
					grid.put(bdp.hash, bdp);
					return bdp;
				} else {
					ChunkDataPoint newVec = new ChunkDataPoint(this, i, k);
					newVec.initFirstTime();
					grid.put(newVec.hash, newVec);
					return newVec;
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
				//NBTTagCompound data = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "ChunkDataDim_" + world.provider.getDimension() + ".dat"));
				dataAllChunks = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "ChunkDataDim_" + world.provider.getDimension() + ".dat"));
				
				/*Collection playerDataCl = data.getTags();
				Iterator it = playerDataCl.iterator();*/
				
				Iterator it = dataAllChunks.getKeySet().iterator();
				
				while (it.hasNext()) {
					String keyName = (String)it.next();
					NBTTagCompound nbt = dataAllChunks.getCompoundTag(keyName);

					//removed to prevent recursive loop for the fix
					/*ChunkDataPoint bdp = this.getChunkData(nbt.getInteger("xCoord"), nbt.getInteger("zCoord"));
					if (bdp != null) {
						bdp.readFromNBT(nbt);
					} else {
						//must have been set to air at some point...
					}*/

					ChunkDataPoint bdp = new ChunkDataPoint(this, nbt.getInteger("xCoord"), nbt.getInteger("zCoord"));
					bdp.initFirstTime();
					bdp.readFromNBT(nbt);
					grid.put(bdp.hash, bdp);
					
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void writeToFile(boolean unloadInstances) {
    	try {
    		
    		Collection playerDataCl = grid.values();
			Iterator it = playerDataCl.iterator();
			
			while (it.hasNext()) {
				ChunkDataPoint bdp = (ChunkDataPoint)it.next();
				dataAllChunks.setTag(""+bdp.hash, bdp.writeToNBT());

				//if were not mass unloading everything later, carefully trim out ones from chunks now unloaded
				if (!unloadInstances) {
					if (world != null) {
						if (!world.isBlockLoaded(new BlockPos(bdp.xCoord * 16, 0, bdp.zCoord * 16))) {
							CULog.dbg("detected saving chunk data for unloaded chunk, removing its data from memory");
							//grid.remove(bdp.hash);
							bdp.cleanup();
							it.remove();
						}
					}
				}
			}
    		
    		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "CoroUtil" + File.separator + "World" + File.separator;
    		
    		//Write out to file
    		if (!(new File(saveFolder).exists())) (new File(saveFolder)).mkdirs();
    		FileOutputStream fos = new FileOutputStream(saveFolder + "ChunkDataDim_" + world.provider.getDimension() + ".dat");
	    	CompressedStreamTools.writeCompressed(dataAllChunks, fos);
	    	fos.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

    	if (unloadInstances) {
    		dataAllChunks = new NBTTagCompound();
		}
	}

    public void tick()
    {
    	
    }
}
