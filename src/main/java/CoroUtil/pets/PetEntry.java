package CoroUtil.pets;

import java.util.UUID;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;

public class PetEntry {

	//TODO: change to player UUID
	//public String ownerName = "";
	public UUID ownerUUID = null;
	
	//public long UUIDMost = -1;
	//public long UUIDLeast = -1;
	public UUID entUUID = null;
	public int ordersMode = 0; //0 = follow, 1 = stay
	
	private MobEntity entRef;
	
	public PetEntry() {
		
	}
	
	public MobEntity getEntity() {
		//need to cleanup/maintain reference here
		if (entRef == null) {
			//hmm
		}
		return entRef;
	}
	
	public void setEntity(MobEntity parEnt) {
		entRef = parEnt;
	}
	
	/*public void initLoad() {
		uUIDObj = new UUID(UUIDMost, UUIDLeast);
	}*/
	
	public void nbtRead(CompoundNBT parNBT) {
		long ownerUUIDMost = parNBT.getLong("ownerUUIDMost");
		long ownerUUIDLeast = parNBT.getLong("ownerUUIDLeast");
		ownerUUID = new UUID(ownerUUIDMost, ownerUUIDLeast);//parNBT.getString("ownerName");
		long UUIDMost = parNBT.getLong("UUIDMost");
		long UUIDLeast = parNBT.getLong("UUIDLeast");
		entUUID = new UUID(UUIDMost, UUIDLeast);
		ordersMode = parNBT.getInteger("ordersMode");
	}
	
	public CompoundNBT nbtWrite() {
		CompoundNBT nbt = new CompoundNBT();
		//nbt.setString("ownerName", ownerName);
		nbt.setLong("ownerUUIDMost", ownerUUID.getMostSignificantBits());
		nbt.setLong("ownerUUIDLeast", ownerUUID.getLeastSignificantBits());
		nbt.setLong("UUIDMost", entUUID.getMostSignificantBits());
		nbt.setLong("UUIDLeast", entUUID.getLeastSignificantBits());
		nbt.setInteger("ordersMode", ordersMode);
		return nbt;
	}
	
}
