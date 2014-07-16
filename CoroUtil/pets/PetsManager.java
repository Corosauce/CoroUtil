package CoroUtil.pets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import CoroUtil.util.CoroUtilFile;

public class PetsManager {

	public List<PetEntry> pets = new ArrayList<PetEntry>();
	
	public PetsManager() {
		
	}
	
	public void addPet(String parOwner, EntityLiving parEnt) {
		PetEntry entry = new PetEntry();
		entry.ownerName = parOwner;
		entry.UUIDLeast = parEnt.getUniqueID().getLeastSignificantBits();
		entry.UUIDMost = parEnt.getUniqueID().getMostSignificantBits();
		entry.uUIDObj = parEnt.getUniqueID();
		addPetEntry(entry);
	}
	
	public void addPetEntry(PetEntry parEntry) {
		pets.add(parEntry);
	}
	
	public void nbtReadFromDisk() {
		FileInputStream fis = null;
		
    	try {
    		String URL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "CoroPets" + File.separator;
    		
    		File file = new File(URL + "tamedPets" + ".dat");
    		if (file.exists()) {
		    	fis = new FileInputStream(file);
		    	
		    	NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fis);
		    	
		    	nbtRead(nbttagcompound);
				
				if (fis != null) {
	    			fis.close();
	    		}
    		}
			
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	} finally {
    		
    		
    	}
	}
	
	public void nbtWriteToDisk() {
		try {
			
			NBTTagCompound nbt = nbtWrite();
			
			String URL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "CoroPets" + File.separator;
			
			File fl = new File(URL);
			if (!fl.exists()) fl.mkdirs();
				
			FileOutputStream fos = new FileOutputStream(URL + "tamedPets" + ".dat");
			
	    	CompressedStreamTools.writeCompressed(nbt, fos);
	    	
	    	fos.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void nbtRead(NBTTagCompound parNBT) {
		Iterator it = parNBT.func_150296_c().iterator();
        while (it.hasNext()) {
        	String tagName = (String) it.next();
        	NBTTagCompound entry = parNBT.getCompoundTag(tagName);
        	
        	PetEntry petEntry = new PetEntry();
        	petEntry.nbtRead(entry);
        	petEntry.initLoad();
        	
        	addPetEntry(petEntry);
        }
	}
	
	public NBTTagCompound nbtWrite() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		for (int i = 0; i < pets.size(); i++) {
			NBTTagCompound nbtEntry = pets.get(i).nbtWrite();
			nbt.setTag("entry_" + i, nbtEntry);
		}
		
		return nbt;
	}
	
	public void reset() {
		pets.clear();
	}
}
