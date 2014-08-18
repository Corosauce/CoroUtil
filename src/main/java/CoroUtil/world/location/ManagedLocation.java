package CoroUtil.world.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import CoroUtil.util.CoroUtilFile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class ManagedLocation {

	public int locationID = -1;
	public int dimID;
	public ChunkCoordinates spawn;
	public boolean hasInit = false;

	public List<EntityLivingBase> listEntities = new ArrayList<EntityLivingBase>();
	public HashMap<Long, Object> lookupIDToTownObject = new HashMap<Long, Object>(); //includes both StructureObjects and ResourceNodes
	public int lastTownObjectIDSet = 1; //command building uses 0, cant increment it from its init
	
	public ManagedLocation() {
		
	}

	public void initData(int parLocation, int parDim, ChunkCoordinates parCoords) {
		locationID = parLocation;
		dimID = parDim;
		spawn = parCoords;
	}
	
	public void initFirstTime() {
		
	}
	
	public void tickUpdate() {
		
	}
	
	public World getWorld() {
		return DimensionManager.getWorld(dimID);
	}
	
	public void addTownObject(StructureObject bb) {
		lookupIDToTownObject.put(bb.ID, bb);
	}
	
	public void removeObject(Object obj) {
		if (obj instanceof EntityLivingBase) {
			listEntities.remove(obj);
		}
		
		if (!(obj instanceof EntityLivingBase)) {
			lookupIDToTownObject.remove(obj);
		}
	}
	
	public void markEntityDied(EntityLivingBase ent) {
		removeObject(ent);
	}
	
	public void addEntity(String unitType, EntityLivingBase ent) {
		listEntities.add(ent);
	}
	
	public void cleanup() {
		listEntities.clear();
		lookupIDToTownObject.clear();
	}
	
	public void writeToNBT(NBTTagCompound var1)
    {
		var1.setString("classname", this.getClass().getCanonicalName());
		
		var1.setInteger("locationID", locationID);
		var1.setInteger("dimID", dimID);
		CoroUtilFile.writeCoords("spawn", spawn, var1);
    }
	
	public void readFromNBT(NBTTagCompound var1)
    {
		hasInit = true;
    	locationID = var1.getInteger("locationID");
    	dimID = var1.getInteger("dimID");
    	spawn = CoroUtilFile.readCoords("spawn", var1);
    }
}
