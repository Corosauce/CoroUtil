package CoroUtil.pets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import CoroUtil.forge.CULog;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import CoroUtil.util.CoroUtilFile;

public class PetsManager {

	private static PetsManager instance = null;
	
	public List<PetEntry> pets = new ArrayList<PetEntry>();
	public HashMap<UUID, PetEntry> lookupUUIDToPet = new HashMap<UUID, PetEntry>();
	
	public static PetsManager instance() {
		if (instance == null) {
			instance = new PetsManager();
		}
		return instance;
	}
	
	public void addPet(UUID parOwner, MobEntity parEnt) {
		PetEntry entry = new PetEntry();
		entry.ownerUUID = parOwner;
		//entry.UUIDLeast = parEnt.getUniqueID().getLeastSignificantBits();
		//entry.UUIDMost = parEnt.getUniqueID().getMostSignificantBits();
		entry.entUUID = parEnt.getUniqueID();
		addPetEntry(entry);
	}
	
	public void addPetEntry(PetEntry parEntry) {
		pets.add(parEntry);
		lookupUUIDToPet.put(parEntry.entUUID, parEntry);
	}
	
	public void removePet(MobEntity parEnt) {
		PetEntry entry = lookupUUIDToPet.get(parEnt.getUniqueID());
		pets.remove(entry);
		lookupUUIDToPet.remove(parEnt.getUniqueID());
	}
	
	public void hookPetInstanceReloaded(CreatureEntity ent) {
		CULog.dbg("pet reloaded: " + ent);
		UUID uuid = ent.getUniqueID();
		initPetsNewInstance(ent);
	}
	
	public void hookPetInstanceUnloaded(CreatureEntity ent) {
		CULog.dbg("pet unloaded: " + ent);
	}
	
	public void initPetsNewInstance(CreatureEntity ent) {
		//do stuff from behavior modifiers
		PetEntry entry = lookupUUIDToPet.get(ent.getUniqueID());
		//TODO: readd 1.8.8
		/*if (entry != null) {
			BehaviorModifier.tameMob(ent, entry.ownerUUID, false);
		} else {
			System.out.println("WARNING!!! failed to find entry for this reloaded mob that is marked tame ");
		}*/
	}
	
	public void nbtReadFromDisk() {
		FileInputStream fis = null;
		
    	try {
    		String URL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "CoroPets" + File.separator;
    		
    		File file = new File(URL + "tamedPets" + ".dat");
    		if (file.exists()) {
		    	fis = new FileInputStream(file);
		    	
		    	CompoundNBT nbttagcompound = CompressedStreamTools.readCompressed(fis);
		    	
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
			
			CompoundNBT nbt = nbtWrite();
			
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
	
	public void nbtRead(CompoundNBT parNBT) {
		Iterator it = parNBT.keySet().iterator();
        while (it.hasNext()) {
        	String tagName = (String) it.next();
        	CompoundNBT entry = parNBT.getCompound(tagName);
        	
        	PetEntry petEntry = new PetEntry();
        	petEntry.nbtRead(entry);
        	//petEntry.initLoad();
        	
        	addPetEntry(petEntry);
        }
	}
	
	public CompoundNBT nbtWrite() {
		CompoundNBT nbt = new CompoundNBT();
		
		for (int i = 0; i < pets.size(); i++) {
			CompoundNBT nbtEntry = pets.get(i).nbtWrite();
			nbt.put("entry_" + i, nbtEntry);
		}
		
		return nbt;
	}
	
	public void reset() {
		pets.clear();
	}
}
