package CoroUtil.pets;

import java.util.UUID;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;

public class PetEntry {

	public String ownerName = "";
	public long UUIDMost = -1;
	public long UUIDLeast = -1;
	public UUID uUIDObj = null;
	public int ordersMode = 0; //0 = follow, 1 = stay
	
	private EntityLiving entRef;
	
	public PetEntry() {
		
	}
	
	public EntityLiving getEntity() {
		//need to cleanup/maintain reference here
		if (entRef == null) {
			//hmm
		}
		return entRef;
	}
	
	public void setEntity(EntityLiving parEnt) {
		entRef = parEnt;
	}
	
	public void initLoad() {
		uUIDObj = new UUID(UUIDLeast, UUIDMost);
	}
	
	public void nbtRead(NBTTagCompound parNBT) {
		ownerName = parNBT.getString("ownerName");
		UUIDMost = parNBT.getLong("UUIDMost");
		UUIDLeast = parNBT.getLong("UUIDLeast");
		ordersMode = parNBT.getInteger("ordersMode");
	}
	
	public NBTTagCompound nbtWrite() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("ownerName", ownerName);
		nbt.setLong("UUIDMost", UUIDMost);
		nbt.setLong("UUIDLeast", UUIDLeast);
		nbt.setInteger("ordersMode", ordersMode);
		return nbt;
	}
	
}
