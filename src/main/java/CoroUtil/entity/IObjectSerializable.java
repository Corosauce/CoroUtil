package CoroUtil.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;
/*
 * Use PacketHelper for creating the packets
 * */
public interface IObjectSerializable {

	public void writeToNBTDisk(CompoundNBT parData);
	public void readFromNBTDisk(CompoundNBT parData);
	
}

