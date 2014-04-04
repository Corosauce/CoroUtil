package CoroUtil.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import CoroUtil.OldUtil;

public class CoroUtilNBT {

	public static NBTTagCompound copyOntoNBT(NBTTagCompound nbtSource, NBTTagCompound nbtDest) {
		NBTTagCompound newNBT = (NBTTagCompound) nbtDest.copy();
		
		//do magic
		try {
			Collection dataCl = nbtSource.getTags();
			Iterator it = dataCl.iterator();
			
			while (it.hasNext()) {
				NBTBase data = (NBTBase)it.next();
				newNBT.setTag(data.getName(), data);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return newNBT;
	}
	
	/*private static NBTTagCompound copyOntoNBTTagCompound nbtSource, NBTTagCompound nbtDest) {
		NBTTagCompound
	}*/
	
	//this should probably be recursive
	/*private static NBTTagCompound copyOntoRecursive(NBTTagCompound nbtSource, NBTTagCompound nbttagcompound)
    {
		try {
			
			
			Collection dataCl = nbtSource.getTags();
			Iterator it = dataCl.iterator();
			
			while (it.hasNext()) {
				NBTBase data = (NBTBase)it.next();
				if (data instanceof NBTTagCompound) {
					NBTTagCompound resultCopy = copyOntoRecursive((NBTTagCompound)data, nbttagcompound);
				}
				String entryName = data.getName();
			}
			
			
			
			
			
			
			
			//nbttagcompound = new NBTTagCompound(this.getName());
			Map tagMap = (Map)c_CoroAIUtil.getPrivateValueSRGMCP(NBTTagCompound.class, nbtSource, "tagMap", "tagMap");
	        Iterator iterator = tagMap.entrySet().iterator();
	
	        while (iterator.hasNext())
	        {
	        	
	            String s = (String)iterator.next();
	            nbttagcompound.setTag(s, ((NBTBase)tagMap.get(s)).copy());
	        }
		} catch (Exception ex) {
			ex.printStackTrace();
		}

        return nbttagcompound;
    }*/
	
	public static void writeCoords(String name, ChunkCoordinates coords, NBTTagCompound nbt) {
    	nbt.setInteger(name + "X", coords.posX);
    	nbt.setInteger(name + "Y", coords.posY);
    	nbt.setInteger(name + "Z", coords.posZ);
    }
    
    public static ChunkCoordinates readCoords(String name, NBTTagCompound nbt) {
    	if (nbt.hasKey(name + "X")) {
    		return new ChunkCoordinates(nbt.getInteger(name + "X"), nbt.getInteger(name + "Y"), nbt.getInteger(name + "Z"));
    	} else {
    		return null;
    	}
    }
}
