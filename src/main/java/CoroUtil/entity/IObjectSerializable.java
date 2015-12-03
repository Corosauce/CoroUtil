package CoroUtil.entity;

import net.minecraft.nbt.NBTTagCompound;
/*
 * Use PacketHelper for creating the packets
 * */
public interface IObjectSerializable {

	public void writeToNBTDisk(NBTTagCompound parData);
	public void readFromNBTDisk(NBTTagCompound parData);
	
}
